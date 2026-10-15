$ErrorActionPreference = 'Stop'
$env:PYTHONUTF8 = '1'
$wizardPython = Join-Path $PSScriptRoot '.venv/Scripts/python.exe'
$wizardScript = Join-Path $PSScriptRoot 'configure.py'
if (Test-Path -LiteralPath $wizardPython) {
    & $wizardPython $wizardScript @args
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    & python $wizardScript @args
} elseif (Get-Command py -ErrorAction SilentlyContinue) {
    & py -3 $wizardScript @args
} else {
    throw 'Python 3.11+ is required. See install/README.md.'
}
exit $LASTEXITCODE
