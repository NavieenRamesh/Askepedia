CREATE TABLE IF NOT EXISTS wiki_article (
    article_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    source_url VARCHAR(1000) NOT NULL,
    content TEXT NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wiki_article_title UNIQUE (title)
);

CREATE INDEX IF NOT EXISTS idx_wiki_article_title ON wiki_article (title);
