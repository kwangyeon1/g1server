$ErrorActionPreference = 'Stop'
$env:PYTHONUTF8 = '1'
$bridgePython = Join-Path $PSScriptRoot '.venv/Scripts/python.exe'
if (-not (Test-Path -LiteralPath $bridgePython)) { throw 'Run install/setup.ps1 -Execute first.' }
& $bridgePython (Join-Path $PSScriptRoot 'bridge/bridge.py') @args
exit $LASTEXITCODE
