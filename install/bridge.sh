#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
export PYTHONUTF8=1
exec "$INSTALL_DIR/.venv/bin/python" "$INSTALL_DIR/bridge/bridge.py" "$@"
