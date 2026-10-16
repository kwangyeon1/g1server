"""Commit-based, incremental BM25 location index. Never returns source bodies."""
from collections import Counter
import argparse
import fnmatch
import hashlib
import json
import math
import os
from pathlib import Path
import re
import sqlite3
import subprocess
import sys
import time

from glossary import matches
from project_profile import EXCLUDED, PATTERNS, SECRETS

INSTALL = Path(__file__).resolve().parent
DESCRIPTION = ('파일·클래스·메서드·테이블 이름을 모를 때만 locate를 사용하세요. '
               '이름을 알면 grep으로 직접 탐색하세요. 반환값은 위치 후보이며 코드 본문은 없습니다. '
               '수정 전에 현재 원문과 호출자·SQL·테스트를 읽으세요. stale 결과의 줄 번호는 과거 위치입니다.')
VERSION = 2
BLOCKED_DIRS = EXCLUDED | {'logs', 'log', 'generated', '.agents', '.aws', '.ssh', 'tmp', '.cache'}
BLOCKED_FILES = SECRETS + ['.mcp.json', '*.yml', '*.yaml', '*.properties', '*config*', '*settings*',
                         '*.log', '*.min.js', '*.min.css', '*.map', '*.lock', '*.g.cs',
                         '*.generated.*', '*_pb2.py', '*.pb.go', '*designer.cs']


def path_matches(name, patterns):
    name = name.replace('\\', '/').casefold()
    return any(fnmatch.fnmatchcase(name, p.casefold())
               or ('/' not in p and fnmatch.fnmatchcase(name.rsplit('/', 1)[-1], p.casefold()))
               or (p.startswith('**/') and fnmatch.fnmatchcase(name, p[3:].casefold()))
               for p in patterns)


def tokens(text):
    words = re.findall(r'[a-zA-Z_][a-zA-Z_0-9]*|[가-힣]+|[0-9]+', text)
    result = []
    for word in words:
        result.append(word.casefold())
        if re.fullmatch('[가-힣]+', word):
            result.extend(word[i:i+2] for i in range(len(word)-1))
            result.extend(word[i:i+3] for i in range(len(word)-2))
        else:
            result.extend(x.casefold() for x in re.findall(r'[A-Z]?[a-z]+|[A-Z]+(?=[A-Z]|$)|[0-9]+', word))
    return result


def symbol(line):
    # Names only: never echo a source line, literal, function body or comment.
    patterns = [r'\b(?:class|interface|enum|def|function|fn|struct|type)\s+([\w]+)',
                r'<(?:select|update|insert|delete|resultMap)\b[^>]*\bid=["\']([\w.]+)',
                r'^\s*(?:public|private|protected|static|async|export)\b[^;={]*?\b([\w]+)\s*\(',
                r'^\s*#+\s+([\w가-힣 ]+)']
    for pattern in patterns:
        found = re.search(pattern, line)
        if found:
            return found.group(1)[:120]
    return None


class Locator:
    def __init__(self, root=None, install=INSTALL):
        self.install = Path(install).resolve()
        self.root = Path(root or self.install.parent).resolve()
        self.config = json.loads((self.install / 'config.json').read_text(encoding='utf-8'))
        self.options = self.config.get('navigation', {})
        self.database = self.install / '.runtime/locate/index.sqlite'

    def reload(self):
        self.config = json.loads((self.install / 'config.json').read_text(encoding='utf-8'))
        self.options = self.config.get('navigation', {})

    def policy(self):
        return hashlib.sha256(json.dumps([VERSION, self.config, sorted(BLOCKED_DIRS), BLOCKED_FILES], sort_keys=True).encode()).hexdigest()

    def status(self):
        self.reload()
        if not self.options.get('locate_enabled', True):
            return {'status': 'DISABLED'}
        try:
            head = self.head()
        except (ValueError, FileNotFoundError):
            return {'status': 'WAITING_GIT_COMMIT'}
        if not self.database.exists():
            return {'status': 'NO_INDEX', 'head': head}
        db = sqlite3.connect(self.database.as_uri() + '?mode=ro', uri=True)
        try:
            metadata = dict(db.execute('SELECT key,value FROM metadata'))
            current = metadata.get('commit') == head and metadata.get('policy') == self.policy()
            return {'status': 'CURRENT' if current else 'STALE_INDEX', 'head': head,
                    'index_commit': metadata.get('commit'),
                    'note': '커밋/설정 기준 상태. 작업 파일의 변경 여부는 결과별 stale로 확인합니다.'}
        finally:
            db.close()

    def git(self, *args):
        result = subprocess.run(['git', '-C', str(self.root), *args], capture_output=True, timeout=60)
        if result.returncode:
            raise ValueError('Git 저장소와 커밋이 필요합니다. git 상태를 확인하세요.')
        return result.stdout

    def head(self):
        if Path(os.fsdecode(self.git('rev-parse', '--show-toplevel')).strip()).resolve() != self.root:
            raise ValueError('하네스는 Git 저장소 루트의 install 폴더에 놓으세요.')
        return self.git('rev-parse', '--verify', 'HEAD^{commit}').decode().strip()

    def allowed(self, name):
        path = Path(name)
        if path.is_absolute() or '..' in path.parts or any(ord(c) < 32 for c in name):
            return False
        if set(path.parts[:-1]) & (BLOCKED_DIRS | set(self.config.get('exclude_dirs', []))):
            return False
        if path_matches(name, BLOCKED_FILES + self.config.get('sensitive_source_globs', [])
                        + self.options.get('exclude_globs', [])):
            return False
        target = self.root / path
        if not target.resolve().is_relative_to(self.root) or any(p.is_symlink() for p in [target, *target.parents] if p != self.root):
            return False
        install_rel = self.install.relative_to(self.root).as_posix() + '/'
        if name.startswith(install_rel):
            return name.startswith(install_rel + 'knowledge/') and name.endswith('.md') and '/hermes/' not in name
        return path_matches(name, self.config.get('source_globs', PATTERNS))

    def connect(self):
        self.database.parent.mkdir(parents=True, exist_ok=True)
        db = sqlite3.connect(self.database, timeout=30)
        db.row_factory = sqlite3.Row
        db.executescript('''CREATE TABLE IF NOT EXISTS metadata(key TEXT PRIMARY KEY, value TEXT);
          CREATE TABLE IF NOT EXISTS files(path TEXT PRIMARY KEY, blob TEXT, sha256 TEXT);
          CREATE TABLE IF NOT EXISTS chunks(path TEXT, start_line INTEGER, end_line INTEGER,
            symbol TEXT, terms TEXT, PRIMARY KEY(path,start_line));''')
        return db

    def update(self):
        # Long-lived MCP servers must honor changed exclusions without restarting.
        self.reload()
        commit = self.head()
        policy = self.policy()
        db = self.connect()
        try:
            db.execute('BEGIN IMMEDIATE')
            meta = dict(db.execute('SELECT key,value FROM metadata'))
            # Policy is checked even when HEAD is unchanged; tightening exclusions purges old rows.
            if meta.get('commit') == commit and meta.get('policy') == policy:
                db.commit()
                return {'status': 'CURRENT', 'index_commit': commit, 'updated_files': 0, 'deleted_files': 0}
            old = {r['path']: r['blob'] for r in db.execute('SELECT path,blob FROM files')}
            files = {}
            for record in self.git('ls-tree', '-r', '-z', commit).split(b'\0'):
                if not record:
                    continue
                header, name = record.split(b'\t', 1)
                mode, kind, blob = header.decode().split()
                name = os.fsdecode(name)
                if mode in ('100644', '100755') and kind == 'blob' and self.allowed(name):
                    files[name] = blob
            if meta.get('policy') != policy:
                db.execute('DELETE FROM chunks')
                db.execute('DELETE FROM files')
                old = {}
            removed = set(old) - set(files)
            for name in removed:
                db.execute('DELETE FROM chunks WHERE path=?', (name,))
                db.execute('DELETE FROM files WHERE path=?', (name,))
            changed = [(name, blob) for name, blob in files.items() if old.get(name) != blob]
            # One process; fetch only changed blobs. Oversize/binary blobs are never tokenized.
            process = subprocess.Popen(['git', '-C', str(self.root), 'cat-file', '--batch'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
            try:
                for name, blob in changed:
                    process.stdin.write((blob + '\n').encode()); process.stdin.flush()
                    header = process.stdout.readline().split()
                    if len(header) != 3 or header[1] != b'blob':
                        raise ValueError('Git blob 응답을 읽을 수 없습니다.')
                    size = int(header[2])
                    limit = min(self.config.get('max_source_bytes', 8388608), 2097152)
                    body = process.stdout.read(size) if size <= limit else None
                    if body is None:
                        remaining = size
                        while remaining:
                            part = process.stdout.read(min(65536, remaining))
                            if not part:
                                raise ValueError('Git blob 응답 중단')
                            remaining -= len(part)
                    process.stdout.read(1)
                    db.execute('DELETE FROM chunks WHERE path=?', (name,))
                    db.execute('DELETE FROM files WHERE path=?', (name,))
                    if body is None or b'\0' in body:
                        continue
                    try:
                        lines = body.decode('utf-8-sig').splitlines()
                    except UnicodeDecodeError:
                        continue
                    if any(re.search(r'(?i)@generated|auto[- ]generated|do not edit', line) for line in lines[:8]):
                        continue
                    db.execute('INSERT INTO files VALUES (?,?,?)', (name, blob, hashlib.sha256(body.replace(b'\r\n', b'\n')).hexdigest()))
                    enclosing = Path(name).stem[:120]
                    labels = []
                    for line in lines:
                        enclosing = symbol(line) or enclosing
                        labels.append(enclosing)
                    for offset in range(0, len(lines), 60):
                        end = min(offset + 80, len(lines))
                        terms = Counter(tokens(name + '\n' + '\n'.join(lines[offset:end])))
                        # The window can cross declarations; label all contained names instead
                        # of incorrectly attributing the whole window to the preceding method.
                        names = list(dict.fromkeys(labels[offset:end]))
                        db.execute('INSERT INTO chunks VALUES (?,?,?,?,?)',
                                   (name, offset+1, end, ' | '.join(names)[:240], json.dumps(terms, ensure_ascii=False)))
            finally:
                process.stdin.close()
                process.stdout.close()
                process.wait(timeout=10)
            for k, v in [('commit', commit), ('policy', policy)]:
                db.execute('INSERT OR REPLACE INTO metadata VALUES (?,?)', (k, v))
            db.commit()
            return {'status': 'INDEXED', 'index_commit': commit, 'updated_files': len(changed), 'deleted_files': len(removed)}
        except BaseException:
            db.rollback()
            raise
        finally:
            db.close()

    def locate(self, query, limit=8):
        if not isinstance(query, str) or not query.strip() or len(query) > 2000:
            raise ValueError('query는 1~2000자의 검색어여야 합니다.')
        if type(limit) is not int or not 1 <= limit <= 20:
            raise ValueError('limit은 1~20의 정수여야 합니다.')
        self.reload()
        if not self.options.get('locate_enabled', True):
            return {'status': 'DISABLED', 'results': []}
        stamp = time.perf_counter()
        update = self.update()
        glossary = matches(self.install, query)
        original = set(tokens(query))
        expanded = set(tokens(' '.join(s for item in glossary for s in item['symbols']))) - original
        db = self.connect()
        try:
            # Consistent read snapshot during concurrent index updates.
            db.execute('BEGIN')
            commit = dict(db.execute('SELECT key,value FROM metadata'))['commit']
            chunks = [dict(r) for r in db.execute('SELECT chunks.*,files.sha256 FROM chunks JOIN files USING(path)') if self.allowed(r['path'])]
        finally:
            db.close()
        for chunk in chunks:
            chunk['terms'] = json.loads(chunk['terms'])
        frequency = Counter(t for c in chunks for t in c['terms'])
        average = sum(sum(c['terms'].values()) for c in chunks) / max(1, len(chunks)) or 1
        ranked = []
        for chunk in chunks:
            terms = chunk['terms']; length = sum(terms.values()); score = 0.0
            for t in original | expanded:
                tf = terms.get(t, 0)
                if tf:
                    score += math.log(1 + (len(chunks)-frequency[t]+.5)/(frequency[t]+.5)) * tf * 2.2 / (tf+1.2*(.25+.75*length/average))
            if score:
                ranked.append((score, chunk))
        results = []; counts = Counter()
        for score, chunk in sorted(ranked, key=lambda x: (-x[0], x[1]['path'], x[1]['start_line'])):
            name = chunk['path']
            if counts[name] >= 2:
                continue
            path = self.root / name
            stale = 'deleted'
            if path.is_file():
                stale = 'modified' if path.stat().st_size > 2097152 or hashlib.sha256(path.read_bytes().replace(b'\r\n', b'\n')).hexdigest() != chunk['sha256'] else None
            result = {k: chunk[k] for k in ('path', 'start_line', 'end_line', 'symbol')}
            result.update(via=['glossary', 'bm25'] if expanded & chunk['terms'].keys() else ['bm25'],
                          index_commit=commit, stale=stale is not None, stale_reason=stale)
            results.append(result); counts[name] += 1
            if len(results) >= limit:
                break
        return {'status': 'FOUND' if results else 'NO_MATCH', 'results': results,
                'index_commit': commit, 'index_update': update,
                'elapsed_ms': round((time.perf_counter()-stamp)*1000, 2),
                'scope': 'Git HEAD의 허용된 텍스트 파일. 미커밋 신규 파일은 grep으로 탐색하세요.',
                'instruction': DESCRIPTION}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project-root')
    sub = parser.add_subparsers(dest='command', required=True)
    sub.add_parser('index')
    locate = sub.add_parser('locate'); locate.add_argument('query'); locate.add_argument('--limit', type=int, default=8)
    args = parser.parse_args()
    try:
        locator = Locator(args.project_root)
        result = locator.update() if args.command == 'index' else locator.locate(args.query, args.limit)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0
    except (OSError, ValueError, sqlite3.Error, subprocess.SubprocessError) as exc:
        print(json.dumps({'status': 'UNAVAILABLE', 'error': str(exc), 'action': 'grep과 현재 원문 읽기를 계속하세요.'}, ensure_ascii=False))
        return 1


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    raise SystemExit(main())
