package com.askepedia.service;

import com.askepedia.entity.Passage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits plain-text Wikipedia article content into clean, sequential,
 * evidence-sized passages suitable for database search and RAG prompts.
 */
@Component
public class PassageSplitter {

    public static final int DEFAULT_MIN_CHUNK_LENGTH = 200;
    public static final int DEFAULT_MAX_CHUNK_LENGTH = 1000;

    // Strips non-content terminal sections (References, See also, External links, etc.)
    private static final Pattern NOISE_SECTIONS_PATTERN = Pattern.compile(
            "(?i)==\\s*(References|See also|External links|Further reading|Notes|Bibliography)\\s*==.*",
            Pattern.DOTALL
    );

    // Normalizes section headings: "== History ==" -> "History:"
    private static final Pattern SECTION_HEADER_PATTERN = Pattern.compile("==+\\s*([^=]+)\\s*==+");

    public List<Passage> split(Long articleId, String rawText) {
        return split(articleId, rawText, DEFAULT_MIN_CHUNK_LENGTH, DEFAULT_MAX_CHUNK_LENGTH);
    }

    public List<Passage> split(Long articleId, String rawText, int minChunkLength, int maxChunkLength) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }

        // 1. Strip reference/citation appendix sections
        String cleanedText = NOISE_SECTIONS_PATTERN.matcher(rawText).replaceAll("").trim();

        // 2. Format section headers as readable section prefixes
        cleanedText = SECTION_HEADER_PATTERN.matcher(cleanedText).replaceAll("\n\n$1:\n");

        // 3. Split by paragraph boundaries
        String[] rawParagraphs = cleanedText.split("\\n\\s*\\n");

        List<String> textChunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String para : rawParagraphs) {
            String trimmedPara = para.trim().replaceAll("\\s+", " ");
            if (trimmedPara.isEmpty()) {
                continue;
            }

            // If a single paragraph is longer than maxChunkLength, split on sentence boundaries
            if (trimmedPara.length() > maxChunkLength) {
                if (!currentChunk.isEmpty()) {
                    textChunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                textChunks.addAll(splitLongParagraph(trimmedPara, maxChunkLength));
                continue;
            }

            // If adding this paragraph exceeds maxChunkLength, flush current accumulator
            if (!currentChunk.isEmpty() && (currentChunk.length() + trimmedPara.length() + 2 > maxChunkLength)) {
                textChunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
            }

            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmedPara);

            // If current accumulator has reached standard chunk size, flush
            if (currentChunk.length() >= minChunkLength) {
                textChunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
            }
        }

        if (!currentChunk.isEmpty()) {
            // Merge tiny trailing snippets into the previous chunk if possible
            if (currentChunk.length() < minChunkLength && !textChunks.isEmpty()) {
                int lastIdx = textChunks.size() - 1;
                String merged = textChunks.get(lastIdx) + "\n\n" + currentChunk.toString().trim();
                textChunks.set(lastIdx, merged);
            } else {
                textChunks.add(currentChunk.toString().trim());
            }
        }

        // 4. Map text chunks to Passage domain records with 1-based sequenceNo
        List<Passage> passages = new ArrayList<>(textChunks.size());
        Instant now = Instant.now();
        for (int i = 0; i < textChunks.size(); i++) {
            passages.add(new Passage(
                    null,
                    articleId,
                    i + 1,
                    textChunks.get(i),
                    now
            ));
        }

        return passages;
    }

    private List<String> splitLongParagraph(String paragraph, int maxChunkLength) {
        List<String> chunks = new ArrayList<>();
        // Split on sentence boundaries (period, question mark, exclamation followed by space)
        String[] sentences = paragraph.split("(?<=[.?!])\\s+");

        StringBuilder chunk = new StringBuilder();
        for (String sentence : sentences) {
            if (!chunk.isEmpty() && (chunk.length() + sentence.length() + 1 > maxChunkLength)) {
                chunks.add(chunk.toString().trim());
                chunk.setLength(0);
            }
            if (!chunk.isEmpty()) {
                chunk.append(" ");
            }
            chunk.append(sentence);
        }
        if (!chunk.isEmpty()) {
            chunks.add(chunk.toString().trim());
        }
        return chunks;
    }
}
