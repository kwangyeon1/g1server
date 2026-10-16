"""Bootstrap the portable harness in the parent project, including an empty directory."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import sys

INSTALL = Path(__file__).resolve().parent


def execute():
    from harness import Harness, quiet
    from agent_setup import plan, apply
    h = Harness(INSTALL.parent, INSTALL)
    interpreter = INSTALL / '.venv' / ('Scripts/python.exe' if sys.platform == 'win32' else 'bin/python')
    changes = plan(INSTALL.parent, INSTALL, interpreter)
    h.setup(True)
    if not h.sources() and not (INSTALL / 'knowledge/source-baseline.json').exists():
        quiet(h.accept_review, 'Bootstrap: no tracked sources exist; only starter documentation is present')
        quiet(h.index)
    apply(INSTALL.parent, INSTALL, changes)
    code = subprocess.run([str(interpreter), str(INSTALL / 'harness.py'), 'verify-install']).returncode
    print('설치 완료. 앱의 MCP/훅 신뢰와 용어 사전 검토는 install/README.md를 확인하세요.')
    return code


def main():
    os.environ['PYTHONUTF8'] = '1'
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--execute', action='store_true', help='현재 설정으로 비대화형 설치')
    mode.add_argument('--tui', action='store_true', help='한글 설치 화면')
    mode.add_argument('--plan', action='store_true', help='변경 없이 계획만 출력')
    args = parser.parse_args()
    if INSTALL.name != 'install':
        raise ValueError('Extract the install folder directly inside the target project')
    if sys.version_info < (3, 11):
        raise ValueError('Python 3.11+ is required')
    if args.tui or (not args.execute and not args.plan and sys.stdin.isatty()):
        from installer import run
        try:
            return run(INSTALL)
        except (EOFError, KeyboardInterrupt):
            print('\n설치 화면을 종료했습니다. 이미 완료된 변경은 유지됩니다.')
            return 130
    if not args.execute:
        print(json.dumps({'status': 'PLAN', 'root': str(INSTALL.parent),
                          'actions': ['Create local venv', 'Merge guidance', 'Generate inventory and search index',
                                      'Merge Codex/Claude glossary hooks and locate MCP'],
                          'runs_project_or_database': False}, ensure_ascii=False, indent=2))
        return 0
    return execute()


if __name__ == '__main__':
    raise SystemExit(main())
