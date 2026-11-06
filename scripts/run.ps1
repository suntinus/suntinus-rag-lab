$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
. "$PSScriptRoot/load-env.ps1"
.\mvnw.cmd spring-boot:run
if($LASTEXITCODE -ne 0){throw 'Application failed'}
