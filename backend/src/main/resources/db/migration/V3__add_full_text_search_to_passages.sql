ALTER TABLE wiki_passage
ADD COLUMN IF NOT EXISTS search_vector tsvector
GENERATED ALWAYS AS (to_tsvector('english', passage_text)) STORED;

CREATE INDEX IF NOT EXISTS idx_wiki_passage_search_vector
ON wiki_passage USING GIN (search_vector);
