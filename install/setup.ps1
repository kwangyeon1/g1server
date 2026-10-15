param([switch]$Execute, [switch]$SkipAgentGuidance)
$ErrorActionPreference = 'Stop'
$setupArgs = @('setup')
if ($Execute) { $setupArgs += '--execute' }
if ($SkipAgentGuidance) { $setupArgs += '--skip-agent-guidance' }
& (Join-Path $PSScriptRoot 'run.ps1') @setupArgs
exit $LASTEXITCODE
