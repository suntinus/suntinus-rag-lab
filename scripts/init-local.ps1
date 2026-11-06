$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (Test-Path .env) { Write-Output '.env already exists; preserved.'; exit 0 }
$dbSecret = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
$apiSecret = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
[IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), "DB_PASSWORD=$dbSecret`nRAG_API_TOKEN=$apiSecret`nEMBEDDING_REVISION=not-pulled`n")
Write-Output 'Created local secrets in ignored .env. Values not printed.'
