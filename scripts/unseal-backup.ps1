param([Parameter(Mandatory)][string]$EncryptedFile,[Parameter(Mandatory)][string]$IdentityFile,[Parameter(Mandatory)][string]$Destination)
$ErrorActionPreference='Stop'
if(Test-Path -LiteralPath $Destination){throw 'Choose a new empty destination; existing paths are not overwritten'}
New-Item -ItemType Directory -Path $Destination | Out-Null
$destinationPath=(Resolve-Path -LiteralPath $Destination).Path
$archive=Join-Path $destinationPath 'snapshot.zip'
age -d -i $IdentityFile -o $archive $EncryptedFile
if($LASTEXITCODE -ne 0){throw 'Decryption failed'}
Expand-Archive -LiteralPath $archive -DestinationPath $destinationPath
Remove-Item -LiteralPath $archive
Write-Output 'Decrypted. Run restore-drill.ps1 against this directory to verify an isolated restoration.'
