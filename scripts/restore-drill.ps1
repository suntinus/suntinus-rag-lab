param([Parameter(Mandatory)][string]$BackupPath)
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$resolved=(Resolve-Path -LiteralPath $BackupPath).Path
$manifest=Get-Content (Join-Path $resolved 'manifest.json') -Raw | ConvertFrom-Json
$dump=Join-Path $resolved 'database.dump'
if((Get-FileHash $dump -Algorithm SHA256).Hash -ne $manifest.sha256){throw 'Backup checksum mismatch'}
$drillDb='rag_restore_'+(Get-Date -Format 'yyyyMMddHHmmssfff')
if($drillDb -notmatch '^rag_restore_\d{17}$'){throw 'Invalid restore target'}
$temporaryDump="/tmp/$drillDb.dump"
docker compose cp $dump "postgres:$temporaryDump"
if($LASTEXITCODE -ne 0){throw 'Copy failed'}
try {
    docker compose exec -T postgres createdb -U rag $drillDb
    if($LASTEXITCODE -ne 0){throw 'Creating isolated restore database failed'}
    docker compose exec -T postgres pg_restore -U rag -d $drillDb --exit-on-error --no-owner --no-privileges $temporaryDump
    if($LASTEXITCODE -ne 0){throw 'Restore failed; source database untouched'}
    docker compose exec -T postgres psql -U rag -d $drillDb -c 'SELECT (SELECT count(*) FROM documents) AS documents, (SELECT count(*) FROM chunks) AS chunks;'
    if($LASTEXITCODE -ne 0){throw 'Restore verification query failed'}
    Write-Output "Restore successful in $drillDb. Database retained for inspection; original rag database untouched."
} finally {
    docker compose exec -T postgres rm -f $temporaryDump
}
