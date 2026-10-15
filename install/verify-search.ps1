$ErrorActionPreference = 'Stop'
$env:PYTHONUTF8 = '1'
& (Join-Path $PSScriptRoot '.venv/Scripts/python.exe') (Join-Path $PSScriptRoot 'bridge/verify_search.py') @args
exit $LASTEXITCODE
