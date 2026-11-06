# Validação local — 2026-11-06

## Evidências

- `mvnw verify`: 14 testes, zero falhas, zero testes ignorados. Inclui Testcontainers com PostgreSQL/pgvector real, filtros por corpus/assunto/espaço vetorial, substituição de trechos e rollback de falha.
- Ambos os adaptadores ingeriram o corpus sintético usando Ollama real.
- Recuperação: fonte esperada presente nas quatro consultas sintéticas (duas perguntas × dois adaptadores).
- Geração com Qwen2.5 1.5B: duas respostas com referências válidas e duas abstenções após falha de formatação, mesmo com uma tentativa adicional. Portanto, geração fundamentada ainda requer calibração; não declarar qualidade aprovada em todo o conjunto.
- Corpus vazio: ambos os adaptadores abstiveram sem geração.
- Uma consulta privada recuperou o documento de contexto consolidado, mas o modelo leve não produziu referência válida. O conteúdo privado e suas respostas não são publicados aqui.
- Backup local: checksum, criptografia/descriptografia, download do snapshot remoto e restauração em banco novo conferidos. Banco original preservado. Chave privada mantida fora do repositório.
- Sintaxe dos scripts PowerShell e configuração Compose verificadas. Código público separado de `.env`, dados, dumps e chaves por exclusões e revisão dos arquivos preparados para commit.
- Imagem Java construída e iniciada no Compose com PostgreSQL e Ollama; health e endpoints autenticados responderam. A avaliação sintética foi repetida dentro do container com os mesmos quatro acertos de recuperação e duas abstenções.

## Limites

Esta é uma avaliação pequena, não um benchmark representativo. Latências variam com carga, cache e inicialização do modelo. Citações sintaticamente válidas não demonstram fidelidade semântica. Resistência a prompt injection, multiusuário, recuperação de desastre sem a máquina original, backup agendado e upload via rclone não foram certificados.

O próximo experimento deve comparar um gerador mais robusto com o baseline leve, usando perguntas rotuladas e avaliação da sustentação de cada afirmação. Preservar a abstenção; não inserir citações automaticamente para mascarar falhas do modelo.
