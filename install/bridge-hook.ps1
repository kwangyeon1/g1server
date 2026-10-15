# Codex invokes this via -File. Keep stdin/stdout as UTF-8 JSON, without pipeline re-encoding.
param([switch]$Unused, [Parameter(ValueFromRemainingArguments=$true)][string[]]$HookArguments)
$env:PYTHONUTF8 = '1'
$bridgePython = Join-Path $PSScriptRoot '.venv/Scripts/python.exe'
& $bridgePython (Join-Path $PSScriptRoot 'bridge/bridge.py') hook --bridge-managed
exit $LASTEXITCODE
