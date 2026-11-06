$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
foreach ($model in @('bge-m3:latest','qwen2.5:1.5b')) {
    docker compose exec -T ollama ollama pull $model
    if ($LASTEXITCODE -ne 0) { throw "Failed to pull $model" }
}
$tags=Invoke-RestMethod 'http://127.0.0.1:11438/api/tags'
$revision=($tags.models | Where-Object name -eq 'bge-m3:latest').digest
if (-not $revision) { throw 'Embedding digest not found' }
$content=Get-Content .env -Raw
$content=[regex]::Replace($content,'(?m)^EMBEDDING_REVISION=.*$',"EMBEDDING_REVISION=$revision")
[IO.File]::WriteAllText((Join-Path (Get-Location) '.env'),$content)
Write-Output 'Models downloaded; embedding digest recorded. Restart app after model changes.'
