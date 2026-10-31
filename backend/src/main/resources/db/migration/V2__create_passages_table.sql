CREATE TABLE IF NOT EXISTS wiki_passage (
    passage_id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    passage_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_passage_article FOREIGN KEY (article_id) REFERENCES wiki_article (article_id) ON DELETE CASCADE,
    CONSTRAINT uq_article_sequence UNIQUE (article_id, sequence_no)
);

CREATE INDEX IF NOT EXISTS idx_wiki_passage_article_id ON wiki_passage (article_id);
