param([Parameter(Mandatory)][string]$BackupPath)
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if(-not(Get-Command age -ErrorAction SilentlyContinue) -or -not(Get-Command age-keygen -ErrorAction SilentlyContinue)){throw 'Install age from https://github.com/FiloSottile/age/releases and add it to PATH'}
New-Item -ItemType Directory -Force private | Out-Null
$key=Join-Path (Get-Location) 'private/backup-key.txt'
if(-not(Test-Path $key)){
    age-keygen -o $key
    if($LASTEXITCODE -ne 0){throw 'Generating backup key failed'}
}
if($IsWindows){
    $identity=[Security.Principal.WindowsIdentity]::GetCurrent().Name
    icacls $key /inheritance:r /grant:r ($identity+':(F)') | Out-Null
    if($LASTEXITCODE -ne 0){throw 'Could not restrict recovery-key access'}
}
$recipient=age-keygen -y $key
if($LASTEXITCODE -ne 0){throw 'Reading public recipient failed'}
$resolved=(Resolve-Path -LiteralPath $BackupPath).Path
$archive="$resolved.zip"
if(Test-Path "$archive.age"){throw 'Encrypted snapshot already exists; refusing overwrite'}
Compress-Archive -LiteralPath (Join-Path $resolved 'database.dump'),(Join-Path $resolved 'manifest.json') -DestinationPath $archive
age -r $recipient -o "$archive.age" $archive
if($LASTEXITCODE -ne 0){throw 'Encryption failed'}
$verification="$resolved.verify.zip"
age -d -i $key -o $verification "$archive.age"
if($LASTEXITCODE -ne 0){throw 'Local decryption verification failed'}
if((Get-FileHash $archive).Hash -ne (Get-FileHash $verification).Hash){throw 'Encrypted round-trip mismatch'}
# Delete only the two exact temporary archives created by this script.
Remove-Item -LiteralPath $archive,$verification
Write-Output "Verified encrypted snapshot: $archive.age"
Write-Output 'Private recovery key: private/backup-key.txt. Store a separate secure copy. Never upload the key with the snapshot.'
