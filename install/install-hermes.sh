#!/usr/bin/env sh
set -eu
INSTALL_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ "${1:-}" != "--execute" ]; then
  echo 'PLAN: download and run official Hermes installer --non-interactive; configure model separately with hermes setup.'
  exit 0
fi
mkdir -p "$INSTALL_DIR/.runtime/downloads"
installer="$INSTALL_DIR/.runtime/downloads/hermes-install.sh"
curl --fail --location --proto '=https' 'https://hermes-agent.nousresearch.com/install.sh' -o "$installer"
if command -v sha256sum >/dev/null 2>&1; then sha256sum "$installer" > "$installer.sha256"; fi
exec bash "$installer" --non-interactive
