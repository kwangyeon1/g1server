param([switch]$Execute)
$ErrorActionPreference = 'Stop'
if ($Execute) {
    & (Join-Path $PSScriptRoot 'setup.ps1') -Execute
    if ($LASTEXITCODE -notin @(0,2)) { exit $LASTEXITCODE }
    & (Join-Path $PSScriptRoot 'bridge.ps1') setup --execute
} else {
    & (Join-Path $PSScriptRoot 'bridge.ps1') setup
}
exit $LASTEXITCODE
