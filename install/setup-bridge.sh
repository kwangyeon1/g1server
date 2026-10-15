#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ "${1:-}" = "--execute" ]; then
  code=0
  sh "$INSTALL_DIR/setup.sh" --execute || code=$?
  [ "$code" -eq 0 ] || [ "$code" -eq 2 ] || exit "$code"
fi
exec sh "$INSTALL_DIR/bridge.sh" setup "$@"
