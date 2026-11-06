$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
foreach ($line in Get-Content (Join-Path $projectRoot '.env')) {
    if ($line -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($Matches[1],$Matches[2],'Process') }
}
