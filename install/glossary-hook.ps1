param([Parameter(ValueFromRemainingArguments=$true)][string[]]$HookArguments)
$env:PYTHONUTF8 = '1'
& (Join-Path $PSScriptRoot '.venv/Scripts/python.exe') (Join-Path $PSScriptRoot 'glossary_hook.py')
exit $LASTEXITCODE
