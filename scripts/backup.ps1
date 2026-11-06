param([string]$CryptRemote)
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$stamp=Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$backupPath=Join-Path (Get-Location) "backups/$stamp"
New-Item -ItemType Directory -Path $backupPath -Force | Out-Null
$containerDump="/tmp/rag-$stamp.dump"
try {
    docker compose exec -T postgres pg_dump -U rag -d rag -Fc --no-owner --no-privileges -f $containerDump
    if($LASTEXITCODE -ne 0){throw 'pg_dump failed'}
    docker compose cp "postgres:$containerDump" "$backupPath/database.dump"
    if($LASTEXITCODE -ne 0){throw 'Copying dump failed'}
} finally {
    docker compose exec -T postgres rm -f $containerDump
}
$hash=(Get-FileHash "$backupPath/database.dump" -Algorithm SHA256).Hash
$manifest=@{createdAt=(Get-Date).ToUniversalTime().ToString('o');sha256=$hash;format='pg_dump custom';database='rag';postgresMajor=17;notes='Contains private corpus, chunks, metadata, vectors and Flyway history. Keep local copies private; remote requires rclone crypt.'}
$manifest | ConvertTo-Json | Set-Content "$backupPath/manifest.json" -Encoding utf8
if($CryptRemote) {
    if($CryptRemote -notmatch '^[a-zA-Z0-9_-]+:$'){throw 'Use a bare crypt remote, e.g. rag-crypt:'}
    if(-not(Get-Command rclone -ErrorAction SilentlyContinue)){throw 'Install rclone and configure a Drive remote wrapped by crypt first. Local backup remains available.'}
    $remoteName=$CryptRemote.TrimEnd(':')
    $remotes=rclone listremotes --long
    if($LASTEXITCODE -ne 0 -or -not($remotes | Where-Object {$_ -match ('^'+[regex]::Escape($remoteName)+':\s+crypt\s*$')})){throw 'Remote must be type crypt. Refusing unencrypted upload.'}
    $destination="$CryptRemote$stamp"
    rclone copy $backupPath $destination --immutable
    if($LASTEXITCODE -ne 0){throw 'Encrypted upload failed'}
    rclone cryptcheck $backupPath $destination --one-way
    if($LASTEXITCODE -ne 0){throw 'Encrypted backup verification failed'}
    Write-Output "Encrypted remote backup verified: $destination"
}
Write-Output "Local backup: $backupPath"
