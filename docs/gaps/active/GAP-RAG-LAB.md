# RAG local — laboratório público

Objetivo: comparar Spring AI e LangChain4j sobre PostgreSQL/pgvector e Ollama, usando corpus sintético público e sessões privadas importadas separadamente; produzir backup criptografado restaurável.

Critérios: ingestão retomável; metadados de origem; filtros antes do top-k; isolamento de corpus; abstenção sem evidência; testes reais de banco; checksum e restauração em banco novo; ausência de corpus e segredos no Git público.

Decisões: Java 21 e Maven Wrapper; aplicação única com portas/adaptadores; busca SQL comum para evitar duas políticas de recuperação; embeddings multilíngues BGE-M3 com digest e 1.024 dimensões; geração local leve; divisão Unicode; exatidão antes de HNSW. A autorização do usuário foi implementar diretamente estas escolhas e usar as sessões unificadas como corpus privado.

Não confundir este laboratório com evolução da produção do ParcAI. Nenhum banco, segredo ou deploy de produção faz parte do escopo.

Exercícios futuros: conjunto de perguntas rotuladas do corpus privado, recall@k/MRR, comparação de limiares, recuperação híbrida, re-ranking, fallback validado para modelo maior, HNSW filtrado, papéis de banco separados, backup agendado com política de retenção aprovada.
