"""Invoke the installed official Hermes CLI, never a replacement agent."""
from __future__ import annotations
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import time


def discover(settings):
    configured = settings.get('hermes_command', [])
    if configured:
        if not isinstance(configured, list) or not all(isinstance(x, str) for x in configured):
            raise ValueError('hermes_command must be an argv array')
        return configured
    exe = shutil.which('hermes')
    if exe:
        return [exe]
    homes = [Path(os.environ.get('LOCALAPPDATA', str(Path.home()))) / 'hermes', Path.home() / '.hermes']
    for home in homes:
        for name in ('hermes.exe', 'hermes.cmd', 'hermes'):
            p = home / 'bin' / name
            if p.exists():
                return [str(p)]
        repo = home / 'hermes-agent'
        python = repo / '.venv' / ('Scripts/python.exe' if os.name == 'nt' else 'bin/python')
        if python.exists() and (repo / 'hermes_cli').is_dir():
            return [str(python), '-m', 'hermes_cli.main']
    return []


def argv(settings, query):
    command = discover(settings)
    if not command:
        raise RuntimeError('HERMES_NOT_INSTALLED: run install-hermes then hermes setup')
    command += ['chat', '--query-file', str(query), '--oneshot', '--format', 'stream-json',
                '--toolsets', 'none', '--ignore-rules', '--max-turns', '2']
    if settings.get('provider'):
        command += ['--provider', settings['provider']]
    if settings.get('model'):
        command += ['--model', settings['model']]
    # subprocess invokes .cmd through cmd.exe on Windows. Only fixed argv/path is used;
    # user prompts are in query files and are never interpolated into shell commands.
    return command


def kill_tree(process):
    if process.poll() is not None:
        return
    if os.name == 'nt':
        subprocess.run(['taskkill', '/PID', str(process.pid), '/T', '/F'], capture_output=True, timeout=10)
    else:
        os.killpg(process.pid, signal.SIGTERM)
    try:
        process.wait(timeout=5)
    except subprocess.TimeoutExpired:
        if os.name != 'nt':
            os.killpg(process.pid, signal.SIGKILL)
        else:
            process.kill()
        process.wait(timeout=5)


def parse_result(path):
    result = None
    for line in Path(path).read_text(encoding='utf-8', errors='replace').splitlines():
        if not line.strip():
            continue
        # Current Hermes prints this known startup diagnostic to stdout even in
        # stream-json mode. Ignore only this exact line; malformed protocol and
        # any other non-JSON output still fail closed.
        if line.strip() == 'Warning: Unknown toolsets: none':
            continue
        item = json.loads(line)
        if item.get('type') == 'tool_use':
            raise ValueError('Unexpected Hermes tool call; review profile must have no tools')
        if item.get('type') == 'result':
            result = item
    if not result or result.get('exit_code') != 0 or result.get('error'):
        raise ValueError('Hermes did not return a successful terminal result')
    text = result.get('text', '').strip()
    if text.startswith('```json') and text.endswith('```'):
        text = text[7:-3].strip()
    elif text.startswith('```') and text.endswith('```'):
        text = text[3:-3].strip()
    data = json.loads(text)
    return data, {k: result.get(k) for k in ('session_id', 'tokens', 'duration_ms')}


def run(settings, query, directory, stop_file):
    directory = Path(directory)
    out, err = directory / 'hermes.stdout.jsonl', directory / 'hermes.stderr.log'
    env = {**os.environ, 'PYTHONUTF8': '1', 'DAEMON_BRIDGE_ORIGIN': 'hermes'}
    with out.open('wb') as stdout, err.open('wb') as stderr:
        process = subprocess.Popen(argv(settings, query), cwd=directory, env=env, stdin=subprocess.DEVNULL,
                                   stdout=stdout, stderr=stderr,
                                   creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0,
                                   start_new_session=os.name != 'nt')
        deadline = time.monotonic() + settings['hermes_timeout_seconds']
        try:
            while process.poll() is None:
                if Path(stop_file).exists():
                    raise RuntimeError('Worker stop requested')
                if time.monotonic() >= deadline:
                    raise TimeoutError('Hermes review timeout')
                if out.stat().st_size + err.stat().st_size > 4 * 1024 * 1024:
                    raise ValueError('Hermes output exceeds 4 MiB')
                time.sleep(0.2)
        except BaseException:
            kill_tree(process)
            raise
    if process.returncode != 0:
        raise RuntimeError(f'HERMES_EXIT_{process.returncode}; inspect local stderr log (not copied into database)')
    if out.stat().st_size + err.stat().st_size > 4 * 1024 * 1024:
        raise ValueError('Hermes output exceeds 4 MiB')
    return parse_result(out)
