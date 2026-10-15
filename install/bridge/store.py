"""Small durable queue. Payloads and model responses never go in SQLite."""
from __future__ import annotations
import contextlib
import hashlib
import json
import os
from pathlib import Path
import sqlite3
import tempfile
import time


def key(*parts):
    return hashlib.sha256(json.dumps(parts, ensure_ascii=False, sort_keys=True).encode()).hexdigest()


def write_json(path, data):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, name = tempfile.mkstemp(prefix=".write-", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")
        os.replace(name, path)
    finally:
        if os.path.exists(name):
            os.unlink(name)


def read_json(path, default=None):
    try:
        return json.loads(Path(path).read_text(encoding="utf-8"))
    except FileNotFoundError:
        return default


class Queue:
    def __init__(self, directory):
        self.directory = Path(directory)
        self.path = self.directory / "queue.sqlite"

    @contextlib.contextmanager
    def connection(self):
        db = sqlite3.connect(self.path, timeout=0.25, isolation_level=None)
        db.row_factory = sqlite3.Row
        try:
            db.execute("PRAGMA busy_timeout=250")
            db.execute("PRAGMA synchronous=FULL")
            yield db
        finally:
            db.close()

    def initialize(self):
        self.directory.mkdir(parents=True, exist_ok=True)
        with self.connection() as db:
            db.execute("PRAGMA journal_mode=WAL")
            db.execute("PRAGMA auto_vacuum=INCREMENTAL")
            db.executescript('''
              CREATE TABLE IF NOT EXISTS jobs (
                id TEXT PRIMARY KEY, session TEXT NOT NULL, turn TEXT NOT NULL,
                state TEXT NOT NULL, generation INTEGER NOT NULL, attempts INTEGER NOT NULL,
                due REAL NOT NULL, updated REAL NOT NULL, lease REAL NOT NULL DEFAULT 0,
                owner TEXT, error TEXT
              );
              CREATE INDEX IF NOT EXISTS jobs_due ON jobs(state,due);
              CREATE TABLE IF NOT EXISTS events (
                id TEXT PRIMARY KEY, job TEXT NOT NULL, at REAL NOT NULL, expires REAL NOT NULL
              );
              CREATE INDEX IF NOT EXISTS event_job ON events(job);
              CREATE INDEX IF NOT EXISTS event_expiry ON events(expires);
              CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY, report TEXT NOT NULL, updated REAL NOT NULL
              );
            ''')

    def enqueue(self, event, settings):
        stamp = time.time()
        session = key(event['session_id'])[:24]
        turn = key(event.get('turn_id') or event['hook_event_name'])[:24]
        job = key(session, turn)[:32]
        eid = key(event)
        location = self.directory / "events" / job / (eid + '.json')
        with self.connection() as db:
            db.execute('BEGIN IMMEDIATE')
            try:
                if db.execute('SELECT 1 FROM events WHERE id=?', (eid,)).fetchone():
                    db.execute('COMMIT')
                    return job, False
                # File first: a crash before commit may orphan a file, but cannot lose an accepted event.
                write_json(location, {"received_at": stamp, "event": event})
                db.execute('INSERT INTO events VALUES (?,?,?,?)',
                           (eid, job, stamp, stamp + settings['dedup_days'] * 86400))
                terminal = event['hook_event_name'] in ('Stop', 'PreCompact', 'Interrupt', 'SessionEnd')
                due = stamp + (settings['settle_seconds'] if terminal else settings['abandoned_turn_seconds'])
                db.execute('''INSERT INTO jobs(id,session,turn,state,generation,attempts,due,updated)
                    VALUES (?,?,?,'PENDING',1,0,?,?)
                    ON CONFLICT(id) DO UPDATE SET generation=generation+1, updated=excluded.updated,
                    due=excluded.due,
                    state=CASE WHEN jobs.state='RUNNING' THEN 'RUNNING' ELSE 'PENDING' END,
                    attempts=CASE WHEN jobs.state='RUNNING' THEN jobs.attempts ELSE 0 END''',
                           (job, session, turn, due, stamp))
                db.execute('COMMIT')
            except BaseException:
                db.execute('ROLLBACK')
                raise
        return job, True

    def claim(self, owner, lease_seconds, max_attempts):
        stamp = time.time()
        with self.connection() as db:
            db.execute('BEGIN IMMEDIATE')
            db.execute("UPDATE jobs SET state=CASE WHEN attempts>=? THEN 'FAILED' ELSE 'PENDING' END, owner=NULL WHERE state='RUNNING' AND lease<?",
                       (max_attempts, stamp))
            row = db.execute("SELECT * FROM jobs WHERE state='PENDING' AND due<=? ORDER BY due,id LIMIT 1", (stamp,)).fetchone()
            if row is None:
                db.execute('COMMIT')
                return None
            db.execute("UPDATE jobs SET state='RUNNING',owner=?,lease=?,attempts=attempts+1 WHERE id=?",
                       (owner, stamp + lease_seconds, row['id']))
            events = [r[0] for r in db.execute('SELECT id FROM events WHERE job=? ORDER BY at,id', (row['id'],))]
            db.execute('COMMIT')
            return {**dict(row), 'owner': owner, 'attempts': row['attempts'] + 1, 'events': events}

    def finish(self, job, report):
        with self.connection() as db:
            db.execute('BEGIN IMMEDIATE')
            row = db.execute('SELECT * FROM jobs WHERE id=? AND owner=? AND state=\'RUNNING\'',
                             (job['id'], job['owner'])).fetchone()
            if row is None:
                db.execute('ROLLBACK')
                raise ValueError('Lost job ownership')
            if not (self.directory / report).is_file():
                db.execute('ROLLBACK')
                raise ValueError('Cannot complete without durable report')
            db.execute('INSERT INTO sessions VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET report=excluded.report,updated=excluded.updated',
                       (job['session'], report, time.time()))
            if row['generation'] == job['generation']:
                db.execute('DELETE FROM jobs WHERE id=?', (job['id'],))
            else:
                db.execute("UPDATE jobs SET state='PENDING',owner=NULL,attempts=0 WHERE id=?", (job['id'],))
            db.execute('COMMIT')

    def fail(self, job, error, max_attempts):
        with self.connection() as db:
            db.execute("UPDATE jobs SET state=?,owner=NULL,error=?,due=?,updated=? WHERE id=? AND owner=?",
                       ('FAILED' if job['attempts'] >= max_attempts else 'PENDING', str(error)[:300],
                        time.time() + min(300, 10 * 2 ** job['attempts']), time.time(), job['id'], job['owner']))

    def retry(self):
        with self.connection() as db:
            return db.execute("UPDATE jobs SET state='PENDING',attempts=0,due=?,error=NULL WHERE state='FAILED'", (time.time(),)).rowcount

    def status(self):
        with self.connection() as db:
            return {"jobs": dict(db.execute('SELECT state,COUNT(*) FROM jobs GROUP BY state')),
                    "dedup_records": db.execute('SELECT COUNT(*) FROM events').fetchone()[0],
                    "sessions": db.execute('SELECT COUNT(*) FROM sessions').fetchone()[0]}

    def latest(self, session):
        with self.connection() as db:
            row = db.execute('SELECT report FROM sessions WHERE id=?', (key(session)[:24],)).fetchone()
            return row[0] if row else None

    def maintain(self, settings):
        stamp = time.time()
        with self.connection() as db:
            db.execute("DELETE FROM jobs WHERE state='FAILED' AND updated<?", (stamp - settings['failed_days'] * 86400,))
            db.execute('DELETE FROM events WHERE expires<? AND job NOT IN (SELECT id FROM jobs)', (stamp,))
            db.execute('DELETE FROM sessions WHERE updated<?', (stamp - settings['session_days'] * 86400,))
            db.execute('PRAGMA wal_checkpoint(TRUNCATE)')
            db.execute('PRAGMA incremental_vacuum(100)')


class ProcessLock:
    """OS lock is released on process crash; does not rely on a reusable PID."""
    def __init__(self, path):
        self.path = Path(path)
        self.file = None

    def __enter__(self):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self.file = self.path.open('a+b')
        self.file.seek(0, os.SEEK_END)
        if self.file.tell() == 0:
            self.file.write(b'0')
            self.file.flush()
        self.file.seek(0)
        try:
            if os.name == 'nt':
                import msvcrt
                msvcrt.locking(self.file.fileno(), msvcrt.LK_NBLCK, 1)
            else:
                import fcntl
                fcntl.flock(self.file.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
        except OSError:
            self.file.close()
            self.file = None
            raise RuntimeError('Another worker owns the project lock') from None
        return self

    def __exit__(self, *args):
        if self.file:
            self.file.seek(0)
            if os.name == 'nt':
                import msvcrt
                msvcrt.locking(self.file.fileno(), msvcrt.LK_UNLCK, 1)
            else:
                import fcntl
                fcntl.flock(self.file.fileno(), fcntl.LOCK_UN)
            self.file.close()
