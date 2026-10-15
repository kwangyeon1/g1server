param([switch]$Execute)
$ErrorActionPreference = 'Stop'
$env:PYTHONUTF8 = '1'
$bootstrapArgs = @((Join-Path $PSScriptRoot 'bootstrap.py'))
if ($Execute) { $bootstrapArgs += '--execute' }
if (Get-Command python -ErrorAction SilentlyContinue) {
    & python -c 'import sys; sys.exit(0 if sys.version_info >= (3,11) else 1)' 2>$null
    if ($LASTEXITCODE -eq 0) {
        & python @bootstrapArgs
        exit $LASTEXITCODE
    }
}
if (Get-Command py -ErrorAction SilentlyContinue) {
    & py -3 @bootstrapArgs
    exit $LASTEXITCODE
}
throw 'Install Python 3.11+ first, then run bootstrap again.'
