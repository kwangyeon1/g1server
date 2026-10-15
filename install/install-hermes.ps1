param([switch]$Execute, [string]$InstallerFile)
$ErrorActionPreference = 'Stop'
$downloadDir = Join-Path $PSScriptRoot '.runtime/downloads'
$officialUrl = 'https://hermes-agent.nousresearch.com/install.ps1'
if (-not $Execute) {
    Write-Output "PLAN: download official Hermes installer from $officialUrl; run -NonInteractive. Model sign-in is a separate hermes setup step."
    exit 0
}
New-Item -ItemType Directory -Force -Path $downloadDir | Out-Null
if (-not $InstallerFile) {
    $InstallerFile = Join-Path $downloadDir 'hermes-install.ps1'
    Invoke-WebRequest -Uri $officialUrl -OutFile $InstallerFile
}
$resolvedInstaller = (Resolve-Path -LiteralPath $InstallerFile).Path
$hash = (Get-FileHash -LiteralPath $resolvedInstaller -Algorithm SHA256).Hash
@{ source=$officialUrl; path=$resolvedInstaller; sha256=$hash; at=[DateTime]::UtcNow.ToString('o') } |
    ConvertTo-Json | Set-Content -LiteralPath (Join-Path $downloadDir 'hermes-installer-receipt.json') -Encoding utf8
& $resolvedInstaller -NonInteractive
exit $LASTEXITCODE
