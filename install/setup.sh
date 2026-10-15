#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec sh "$INSTALL_DIR/run.sh" setup "$@"
