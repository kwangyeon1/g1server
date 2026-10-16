"""Preserving, project-local Codex/Claude hook and MCP configuration merges."""
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import tomllib
from harness import read_json, write_json

SERVER = 'project-harness-locate'
START, END = '# project-harness-locate:start', '# project-harness-locate:end'


def managed(handler):
    command = handler.get('command', '')
    # Retain the retired marker only to remove old managed hooks on reinstallation.
    return '--bridge-managed' in command or '--glossary-managed' in command


def hook_command(install, python):
    flag = '--glossary-managed'
    if os.name == 'nt':
        script = install / 'glossary-hook.ps1'
        return f'powershell.exe -NoProfile -ExecutionPolicy Bypass -File "{script}" {flag}'
    script = install / 'glossary_hook.py'
    return f'{shlex.quote(str(python))} {shlex.quote(str(script))} {flag}'


def merge_hooks(data, install, python, claude=False):
    if not isinstance(data, dict) or not isinstance(data.get('hooks', {}), dict):
        raise ValueError('기존 훅 설정 형식을 확인하세요.')
    hooks = data.setdefault('hooks', {})
    for event, groups in list(hooks.items()):
        cleaned = []
        for group in groups:
            item = dict(group)
            item['hooks'] = [h for h in group.get('hooks', []) if not managed(h)]
            if item['hooks']:
                cleaned.append(item)
        if cleaned:
            hooks[event] = cleaned
        else:
            hooks.pop(event, None)
    handler = {'type': 'command', 'command': hook_command(install, python), 'timeout': 5}
    if not claude:
        handler['additionalContextLimit'] = 1500
    hooks.setdefault('UserPromptSubmit', []).append({'hooks': [handler]})
    return data


def merge_codex_mcp(old, python, script):
    parsed = tomllib.loads(old)
    if old.count(START) != old.count(END) or old.count(START) > 1:
        raise ValueError('Codex MCP 관리 블록이 손상되었습니다.')
    if START in old and old.index(START) > old.index(END):
        raise ValueError('Codex MCP 관리 블록 순서가 잘못되었습니다.')
    if SERVER in parsed.get('mcp_servers', {}) and START not in old:
        raise ValueError('같은 이름의 사용자 MCP 설정이 있습니다: ' + SERVER)
    # Remove only our delimited block; TOML remains otherwise byte-for-byte intact.
    clean = re.sub(re.escape(START) + r'.*?' + re.escape(END) + r'\n?', '', old, flags=re.S)
    block = (START + '\n[mcp_servers.' + SERVER + ']\ncommand = ' + json.dumps(str(python))
             + '\nargs = [' + json.dumps(str(script)) + ']\n' + END + '\n')
    result = clean.rstrip() + '\n\n' + block
    tomllib.loads(result)
    return result


def plan(root, install, python):
    root, install = Path(root), Path(install)
    paths = ['.codex/hooks.json', '.claude/settings.json', '.mcp.json', '.codex/config.toml']
    for relative in paths:
        target = root / relative
        if target.is_symlink() or target.parent.is_symlink() or not target.resolve().is_relative_to(root):
            raise ValueError('설정 파일의 심볼릭 링크를 지원하지 않습니다: ' + relative)
    changes = {}
    for relative, claude in [('.codex/hooks.json', False), ('.claude/settings.json', True)]:
        value = merge_hooks(read_json(root / relative, {}), install, python, claude)
        changes[relative] = json.dumps(value, ensure_ascii=False, indent=2) + '\n'
    mcp = read_json(root / '.mcp.json', {})
    servers = mcp.setdefault('mcpServers', {})
    old_server = servers.get(SERVER)
    script = install / 'locate_mcp.py'
    expected = {'command': str(python), 'args': [str(script)]}
    if old_server and old_server != expected:
        raise ValueError('같은 이름의 다른 Claude MCP 설정이 있습니다: ' + SERVER)
    servers[SERVER] = expected
    changes['.mcp.json'] = json.dumps(mcp, ensure_ascii=False, indent=2) + '\n'
    path = root / '.codex/config.toml'
    changes['.codex/config.toml'] = merge_codex_mcp(path.read_text(encoding='utf-8') if path.exists() else '', python, script)
    return changes


def apply(root, install, changes):
    root, install = Path(root), Path(install)
    for relative, text in changes.items():
        path = root / relative
        original = path.read_bytes() if path.exists() else None
        if original == text.encode('utf-8'):
            continue
        if original is not None:
            backup = install / '.runtime/agent-backups' / (hashlib.sha256(original).hexdigest()[:16]) / relative
            backup.parent.mkdir(parents=True, exist_ok=True)
            backup.write_bytes(original)
        # Atomic replacement using the shared JSON writer for JSON; TOML uses a sibling temp.
        if path.suffix == '.json':
            write_json(path, json.loads(text))
        else:
            from configure import atomic_text
            atomic_text(path, text)
