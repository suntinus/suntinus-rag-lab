CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE documents (
    dataset varchar(64) NOT NULL,
    source varchar(200) NOT NULL,
    title varchar(400) NOT NULL,
    topic varchar(100) NOT NULL,
    source_date date NOT NULL,
    kind varchar(32) NOT NULL CHECK (kind IN ('historical-extract','curated-context','synthetic')),
    content_hash varchar(64) NOT NULL,
    embedding_space varchar(300) NOT NULL,
    backend varchar(20) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY(dataset,source)
);
CREATE TABLE chunks (
    dataset varchar(64) NOT NULL,
    source varchar(200) NOT NULL,
    chunk_index integer NOT NULL,
    content text NOT NULL,
    embedding vector(1024) NOT NULL,
    PRIMARY KEY(dataset,source,chunk_index),
    FOREIGN KEY(dataset,source) REFERENCES documents(dataset,source) ON DELETE CASCADE
);
CREATE INDEX documents_topic_idx ON documents(dataset,topic);
-- Exact cosine search initially: correct filtered top-k for this small lab corpus.
-- Add/evaluate HNSW only after measuring filtered recall and EXPLAIN ANALYZE.
