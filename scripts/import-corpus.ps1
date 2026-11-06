param(
    [Parameter(Mandatory)][string]$CorpusPath,
    [string]$Dataset='codex-private',
    [ValidateSet('spring-ai','langchain4j')][string]$Backend='spring-ai',
    [string]$MainMemoryPath,
    [int]$Limit=0
)
$ErrorActionPreference='Stop'
. "$PSScriptRoot/load-env.ps1"
$headers=@{Authorization="Bearer $env:RAG_API_TOKEN"}
$count=0;$updated=0;$unchanged=0;$chunks=0
foreach($line in [IO.File]::ReadLines((Resolve-Path -LiteralPath $CorpusPath))) {
    if([string]::IsNullOrWhiteSpace($line)){continue}
    $record=$line | ConvertFrom-Json -AsHashtable
    $record['dataset']=$Dataset
    $body=$record | ConvertTo-Json -Depth 6 -Compress
    $result=Invoke-RestMethod "http://127.0.0.1:8110/api/$Backend/documents" -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 600
    $count++;$chunks+=$result.chunks
    if($result.unchanged){$unchanged++}else{$updated++}
    if($count % 25 -eq 0){Write-Output "Documents: $count; updated: $updated; unchanged: $unchanged; new chunks: $chunks"}
    if($Limit -gt 0 -and $count -ge $Limit){break}
}
if($MainMemoryPath) {
    $record=@{dataset=$Dataset;source='curated-main-memory';title='Memória pessoal consolidada';topic='00-perfil-contexto';date='2026-11-06';kind='curated-context';text=(Get-Content -LiteralPath $MainMemoryPath -Raw)}
    $body=$record|ConvertTo-Json -Compress
    $result=Invoke-RestMethod "http://127.0.0.1:8110/api/$Backend/documents" -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 600
    $count++;$chunks+=$result.chunks
    if($result.unchanged){$unchanged++}else{$updated++}
}
[pscustomobject]@{documents=$count;updated=$updated;unchanged=$unchanged;newChunks=$chunks;dataset=$Dataset}
