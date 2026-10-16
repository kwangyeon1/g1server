"""Shared Codex/Claude UserPromptSubmit hook: glossary matching only."""
import json
from pathlib import Path
import sys
from glossary import context

INSTALL = Path(__file__).resolve().parent


def respond(raw, install=INSTALL):
    if not isinstance(raw, dict) or raw.get('hook_event_name') != 'UserPromptSubmit':
        return {}
    root = Path(install).resolve().parent
    if not raw.get('cwd') or not Path(raw['cwd']).resolve().is_relative_to(root):
        return {}
    config = json.loads((Path(install) / 'config.json').read_text(encoding='utf-8'))
    if not config.get('navigation', {}).get('glossary_hook_enabled', True):
        return {}
    prompt = raw.get('prompt', '')
    if not isinstance(prompt, str):
        return {}
    text = context(install, prompt)
    return {'hookSpecificOutput': {'hookEventName': 'UserPromptSubmit', 'additionalContext': text}} if text else {}


def main():
    try:
        raw = sys.stdin.buffer.read(131073)
        result = respond(json.loads(raw.decode('utf-8-sig'))) if len(raw) <= 131072 else {}
    except (OSError, ValueError, TypeError, KeyError):
        result = {}  # A broken optional glossary must not block a user prompt.
    print(json.dumps(result, ensure_ascii=False))


if __name__ == '__main__':
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    main()
