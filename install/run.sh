#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
export PYTHONUTF8=1
if [ -x "$INSTALL_DIR/.venv/bin/python" ]; then
  exec "$INSTALL_DIR/.venv/bin/python" "$INSTALL_DIR/harness.py" "$@"
fi
for candidate in python3 python; do
  if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c 'import sys; sys.exit(0 if sys.version_info >= (3,11) else 1)' >/dev/null 2>&1; then
    exec "$candidate" "$INSTALL_DIR/harness.py" "$@"
  fi
done
echo 'Python 3.11+ not found. See install/docs/linux.md.' >&2
exit 1
