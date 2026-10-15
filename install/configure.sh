#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
export PYTHONUTF8=1
if [ -x "$INSTALL_DIR/.venv/bin/python" ]; then
  exec "$INSTALL_DIR/.venv/bin/python" "$INSTALL_DIR/configure.py" "$@"
fi
exec python3 "$INSTALL_DIR/configure.py" "$@"
