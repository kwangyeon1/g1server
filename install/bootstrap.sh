#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
export PYTHONUTF8=1
exec python3 "$INSTALL_DIR/bootstrap.py" "$@"
