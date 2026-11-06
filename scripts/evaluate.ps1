param([string]$Dataset='demo-spring')
$ErrorActionPreference='Stop'
. "$PSScriptRoot/load-env.ps1"
$headers=@{Authorization="Bearer $env:RAG_API_TOKEN"}
$results=@()
foreach($backend in @('spring-ai','langchain4j')) {
    $cases=@(
        @{question='É obrigatório dividir o pagamento do evento entre participantes?';topic='eventos';expected='synthetic-001'},
        @{question='Os testes locais comprovam implantação em produção?';topic='engenharia';expected='synthetic-002'}
    )
    foreach($case in $cases) {
        $body=@{dataset=$Dataset;question=$case.question;topic=$case.topic;topK=3;minScore=0.3}|ConvertTo-Json -Compress
        $answer=Invoke-RestMethod "http://127.0.0.1:8110/api/$backend/query" -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 240
        $hit=@($answer.sources | Where-Object source -eq $case.expected).Count -gt 0
        if(-not $hit){throw "Retrieval regression: $backend / $($case.expected)"}
        $results += [pscustomobject]@{backend=$backend;expectedSource=$case.expected;retrievalHit=$hit;abstained=$answer.abstained;elapsedMs=$answer.elapsedMs;answer=$answer.answer}
    }
    $empty=@{dataset='empty-evaluation';question='Qual o resultado?';topK=3;minScore=0.3}|ConvertTo-Json
    $answer=Invoke-RestMethod "http://127.0.0.1:8110/api/$backend/query" -Method Post -Headers $headers -ContentType 'application/json' -Body $empty -TimeoutSec 240
    if(-not $answer.abstained -or $answer.sources.Count -ne 0){throw 'Missing abstention for empty corpus'}
}
$results | ConvertTo-Json -Depth 5
