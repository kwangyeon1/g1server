$ErrorActionPreference = 'Stop'
$env:PYTHONUTF8 = '1'
$venvPython = Join-Path $PSScriptRoot '.venv/Scripts/python.exe'
if (Test-Path -LiteralPath $venvPython) {
    & $venvPython (Join-Path $PSScriptRoot 'harness.py') @args
    exit $LASTEXITCODE
}
if (Get-Command python -ErrorAction SilentlyContinue) {
    $probe = & python -c 'import sys; sys.exit(0 if sys.version_info >= (3,11) else 1)' 2>&1
    if ($LASTEXITCODE -eq 0) {
        & python (Join-Path $PSScriptRoot 'harness.py') @args
        exit $LASTEXITCODE
    }
}
if (Get-Command py -ErrorAction SilentlyContinue) {
    & py -3 (Join-Path $PSScriptRoot 'harness.py') @args
    exit $LASTEXITCODE
}
throw 'Python 3.11+ was not found. See install/docs/windows.md.'
