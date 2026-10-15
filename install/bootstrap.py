"""Bootstrap the portable harness in the parent project, including an empty directory."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import sys

INSTALL = Path(__file__).resolve().parent


def main():
    os.environ['PYTHONUTF8'] = '1'
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--execute', action='store_true')
    args = parser.parse_args()
    if INSTALL.name != 'install':
        raise ValueError('Extract the install folder directly inside the target project')
    if sys.version_info < (3, 11):
        raise ValueError('Python 3.11+ is required')
    if not args.execute:
        print(json.dumps({'status': 'PLAN', 'root': str(INSTALL.parent),
                          'actions': ['Create local venv', 'Merge guidance', 'Generate inventory and search index',
                                      'Initialize bridge queue and merge hooks'],
                          'hermes_install_and_login': 'Separate explicit steps',
                          'runs_project_or_database': False}, ensure_ascii=False, indent=2))
        return 0
    from harness import Harness, quiet
    h = Harness(INSTALL.parent, INSTALL)
    h.setup(True)
    # No source claims are approved when there are no tracked files.
    if not h.sources() and not (INSTALL / 'knowledge/source-baseline.json').exists():
        quiet(h.accept_review, 'Bootstrap: no tracked sources exist; only starter documentation is present')
        quiet(h.index)
    interpreter = INSTALL / '.venv' / ('Scripts/python.exe' if sys.platform == 'win32' else 'bin/python')
    subprocess.run([str(interpreter), str(INSTALL / 'bridge/bridge.py'), 'setup', '--execute'], check=True)
    code = subprocess.run([str(interpreter), str(INSTALL / 'harness.py'), 'verify-install']).returncode
    print('Bootstrap complete. Review install/README.md for source review, Hermes sign-in and Codex hook trust.')
    return code  # 2 = installation complete, source documentation review required.


if __name__ == '__main__':
    raise SystemExit(main())
