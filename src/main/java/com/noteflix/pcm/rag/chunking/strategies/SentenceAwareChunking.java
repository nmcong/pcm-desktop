package com.noteflix.pcm.rag.chunking.strategies;

import com.noteflix.pcm.rag.chunking.api.ChunkingStrategy;
import com.noteflix.pcm.rag.chunking.core.DocumentChunk;
import com.noteflix.pcm.rag.model.RAGDocument;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sentence-aware chunking strategy.
 *
 * <p>Splits documents into chunks while preserving sentence boundaries. This strategy ensures
 * that chunks end at natural sentence breaks, providing better semantic coherence compared to
 * fixed-size chunking.
 *
 * <p>Ideal for:
 * - Literary texts, articles, documentation
 * - Documents where sentence integrity matters
 * - Improved readability and comprehension
 * - Better semantic search results
 *
 * <p>Features:
 * - Respects sentence boundaries (., !, ?)
 * - Handles common abbreviations (Dr., Mr., etc.)
 * - Configurable target chunk size with flexibility
 * - Quality scoring based on sentence completeness
 * - Smart overlap management at sentence level
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SentenceAwareChunking implements ChunkingStrategy {

    // Advanced sentence detection pattern
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
            // Match sentence endings but exclude common abbreviations
            "(?<![A-Z][a-z]\\.)(?<![A-Z]\\.)(?<=\\.|\\!|\\?)\\s+(?=[A-Z])|"
                    // Handle special cases like quotations
                    + "(?<=[.!?][\"'])\\s+(?=[A-Z])|"
                    // Handle newlines as sentence breaks in some contexts
                    + "(?<=\\.|\\!|\\?)\\n+(?=[A-Z])",
            Pattern.MULTILINE
    );
    // Common abbreviations to avoid breaking
    private static final Pattern ABBREVIATION_PATTERN = Pattern.compile(
            "\\b(?:Dr|Mr|Mrs|Ms|Prof|Sr|Jr|Inc|Ltd|Corp|Co|etc|vs|i\\.e|e\\.g|cf|al|et)\\.",
            Pattern.CASE_INSENSITIVE
    );
    private final int targetChunkSize;
    private final int overlapSize;
    private final double sizeTolerance;

    /**
     * Create sentence-aware chunking with target size and overlap.
     *
     * @param targetChunkSize Target chunk size (flexible)
     * @param overlapSize     Overlap size in characters
     * @param sizeTolerance   Size tolerance (0.0-1.0), how much chunks can deviate from target
     */
    public SentenceAwareChunking(int targetChunkSize, int overlapSize, double sizeTolerance) {
        validateConfig(targetChunkSize, overlapSize);
        if (sizeTolerance < 0.0 || sizeTolerance > 1.0) {
            throw new IllegalArgumentException("Size tolerance must be between 0.0 and 1.0");
        }

        this.targetChunkSize = targetChunkSize;
        this.overlapSize = overlapSize;
        this.sizeTolerance = sizeTolerance;
    }

    /**
     * Create default sentence-aware chunking (1000 chars target, 200 overlap, 0.3 tolerance).
     */
    public static SentenceAwareChunking defaults() {
        return new SentenceAwareChunking(1000, 200, 0.3);
    }

    /**
     * Create strict sentence chunking with low tolerance.
     */
    public static SentenceAwareChunking strict() {
        return new SentenceAwareChunking(800, 100, 0.1);
    }

    /**
     * Create flexible sentence chunking with high tolerance.
     */
    public static SentenceAwareChunking flexible() {
        return new SentenceAwareChunking(1200, 300, 0.5);
    }

    @Override
    public List<DocumentChunk> chunk(RAGDocument document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String content = document.getContent();

        if (content == null || content.isEmpty()) {
            log.warn("Document {} has empty content", document.getId());
            return chunks;
        }

        // Find sentence boundaries
        List<SentenceBoundary> sentences = findSentences(content);
        if (sentences.isEmpty()) {
            log.warn("No sentences found in document {}", document.getId());
            return createFallbackChunks(document, content);
        }

        // Group sentences into chunks
        List<SentenceGroup> groups = groupSentences(sentences, content);

        // Convert groups to chunks
        int index = 0;
        String previousChunkId = null;

        for (SentenceGroup group : groups) {
            String chunkContent = content.substring(group.startPos, group.endPos);
            String currentChunkId = document.getId() + "_chunk_" + index;

            // Calculate quality metrics
            double qualityScore = calculateQualityScore(chunkContent, group);
            double coherenceScore = calculateCoherenceScore(group);
            double densityScore = calculateDensityScore(chunkContent);

            // Build enhanced chunk
            DocumentChunk.DocumentChunkBuilder builder = DocumentChunk.builder()
                    .chunkId(currentChunkId)
                    .documentId(document.getId())
                    .content(chunkContent.trim())
                    .index(index)
                    .startPosition(group.startPos)
                    .endPosition(group.endPos)

                    // Document metadata
                    .documentTitle(document.getTitle())
                    .documentType(document.getType())
                    .sourcePath(document.getSourcePath())
                    .documentTimestamp(document.getIndexedAt())

                    // Chunking metadata
                    .chunkingStrategy(getStrategyName())
                    .chunkSizeChars(chunkContent.length())
                    .overlapSize(group.overlapChars)
                    .hasOverlapBefore(index > 0)
                    .hasOverlapAfter(index < groups.size() - 1)

                    // Quality metrics
                    .qualityScore(qualityScore)
                    .coherenceScore(coherenceScore)
                    .densityScore(densityScore)

                    // Context linking
                    .previousChunkId(previousChunkId);

            // Add sentence-specific metadata
            DocumentChunk chunk = builder.build();
            chunk.addMetadata("sentence_count", String.valueOf(group.sentences.size()));
            chunk.addMetadata("avg_sentence_length", String.valueOf(group.avgSentenceLength));
            chunk.addMetadata("chunk_type", "sentence_aware");

            // Update previous chunk's next link
            if (index > 0 && !chunks.isEmpty()) {
                DocumentChunk prevChunk = chunks.get(chunks.size() - 1);
                prevChunk.setNextChunkId(currentChunkId);
            }

            chunks.add(chunk);

            // Prepare for next iteration
            previousChunkId = currentChunkId;
            index++;
        }

        log.debug("Sentence-aware chunked document {} into {} chunks from {} sentences",
                document.getId(), chunks.size(), sentences.size());

        return chunks;
    }

    @Override
    public int getChunkSize() {
        return targetChunkSize;
    }

    @Override
    public int getOverlapSize() {
        return overlapSize;
    }

    @Override
    public String getStrategyName() {
        return "SentenceAware";
    }

    @Override
    public String getDescription() {
        return String.format(
                "Sentence-aware chunking with %d character target, %d overlap, %.1f%% tolerance",
                targetChunkSize, overlapSize, sizeTolerance * 100);
    }

    @Override
    public double estimateQuality(RAGDocument document) {
        if (document.getContent() == null || document.getContent().isEmpty()) {
            return 0.0;
        }

        String content = document.getContent();
        List<SentenceBoundary> sentences = findSentences(content);

        if (sentences.isEmpty()) {
            return 0.2; // Very low quality if no sentences found
        }

        // Quality factors:
        double sentenceDensity = (double) sentences.size() / content.length() * 1000; // per 1000 chars
        double avgSentenceLength = (double) content.length() / sentences.size();
        double structureScore = Math.min(1.0, sentenceDensity / 5.0); // Normalize to 0-1
        double lengthScore = 1.0 - Math.abs(avgSentenceLength - 100) / 200.0; // Ideal ~100 chars/sentence

        return Math.max(0.0, (structureScore * 0.6 + lengthScore * 0.4));
    }

    @Override
    public boolean isSuitableFor(RAGDocument document) {
        if (document.getContent() == null) {
            return false;
        }

        String content = document.getContent();
        List<SentenceBoundary> sentences = findSentences(content);

        // Suitable if:
        // - Has reasonable number of sentences
        // - Average sentence length is reasonable (20-500 chars)
        if (sentences.size() < 3) {
            return false; // Too few sentences
        }

        double avgLength = (double) content.length() / sentences.size();
        return avgLength >= 20 && avgLength <= 500;
    }

    // === Private Helper Methods ===

    private List<SentenceBoundary> findSentences(String content) {
        List<SentenceBoundary> sentences = new ArrayList<>();

        // Pre-process to protect abbreviations
        String processedContent = protectAbbreviations(content);

        Matcher matcher = SENTENCE_PATTERN.matcher(processedContent);
        int lastEnd = 0;

        while (matcher.find()) {
            int start = lastEnd;
            int end = matcher.start();

            if (end > start) {
                String sentence = content.substring(start, end).trim();
                if (!sentence.isEmpty()) {
                    sentences.add(new SentenceBoundary(start, end, sentence));
                }
            }
            lastEnd = end;
        }

        // Add final sentence
        if (lastEnd < content.length()) {
            String sentence = content.substring(lastEnd).trim();
            if (!sentence.isEmpty()) {
                sentences.add(new SentenceBoundary(lastEnd, content.length(), sentence));
            }
        }

        return sentences;
    }

    private String protectAbbreviations(String content) {
        // Replace common abbreviations with placeholders to prevent false sentence splits
        return ABBREVIATION_PATTERN.matcher(content).replaceAll(m ->
                m.group().replace(".", "§"));
    }

    private List<SentenceGroup> groupSentences(List<SentenceBoundary> sentences, String content) {
        List<SentenceGroup> groups = new ArrayList<>();

        int minSize = (int) (targetChunkSize * (1.0 - sizeTolerance));
        int maxSize = (int) (targetChunkSize * (1.0 + sizeTolerance));

        int currentStart = 0;
        int currentSize = 0;
        List<SentenceBoundary> currentSentences = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            SentenceBoundary sentence = sentences.get(i);
            int sentenceLength = sentence.text.length();

            // If adding this sentence would exceed max size, finalize current group
            if (!currentSentences.isEmpty() && currentSize + sentenceLength > maxSize) {
                // Create group from current sentences
                groups.add(createSentenceGroup(currentSentences, content, groups.size()));

                // Start new group with overlap
                currentSentences = new ArrayList<>();
                currentStart = calculateOverlapStart(sentences, i, groups.get(groups.size() - 1));
                currentSize = 0;
            }

            currentSentences.add(sentence);
            currentSize += sentenceLength;

            // If we've reached minimum size and this is a good stopping point, we can finish
            if (currentSize >= minSize && isGoodStoppingPoint(sentence)) {
                // Look ahead to see if we should include next sentence
                if (i + 1 < sentences.size()) {
                    SentenceBoundary nextSentence = sentences.get(i + 1);
                    if (currentSize + nextSentence.text.length() <= maxSize) {
                        continue; // Include next sentence
                    }
                }

                // Finalize current group
                groups.add(createSentenceGroup(currentSentences, content, groups.size()));
                currentSentences = new ArrayList<>();
                currentSize = 0;
            }
        }

        // Add remaining sentences
        if (!currentSentences.isEmpty()) {
            groups.add(createSentenceGroup(currentSentences, content, groups.size()));
        }

        return groups;
    }

    private SentenceGroup createSentenceGroup(List<SentenceBoundary> sentences, String content, int groupIndex) {
        if (sentences.isEmpty()) {
            throw new IllegalArgumentException("Cannot create group from empty sentences");
        }

        int startPos = sentences.get(0).start;
        int endPos = sentences.get(sentences.size() - 1).end;
        double avgLength = sentences.stream().mapToInt(s -> s.text.length()).average().orElse(0.0);

        return new SentenceGroup(startPos, endPos, sentences, avgLength, 0);
    }

    private int calculateOverlapStart(List<SentenceBoundary> sentences, int currentIndex, SentenceGroup previousGroup) {
        // Calculate how many characters we want to overlap
        int targetOverlapChars = Math.min(overlapSize, previousGroup.endPos - previousGroup.startPos);
        int overlapStart = previousGroup.endPos - targetOverlapChars;

        // Find the sentence that contains or starts closest to our overlap point
        for (int i = currentIndex - 1; i >= 0; i--) {
            if (sentences.get(i).start <= overlapStart) {
                return sentences.get(i).start;
            }
        }

        return sentences.get(Math.max(0, currentIndex - 1)).start;
    }

    private boolean isGoodStoppingPoint(SentenceBoundary sentence) {
        // A sentence is a good stopping point if:
        // 1. It ends with strong punctuation
        // 2. It's not too short (avoid stopping on fragments)
        String text = sentence.text.trim();
        return text.length() >= 20 &&
                (text.endsWith(".") || text.endsWith("!") || text.endsWith("?"));
    }

    private List<DocumentChunk> createFallbackChunks(RAGDocument document, String content) {
        // Fallback to fixed-size chunking if sentence detection fails
        log.warn("Falling back to fixed-size chunking for document {}", document.getId());
        FixedSizeChunking fallback = new FixedSizeChunking(targetChunkSize, overlapSize);
        return fallback.chunk(document);
    }

    private double calculateQualityScore(String chunkContent, SentenceGroup group) {
        // Quality based on sentence completeness and size conformance
        double sizeScore = 1.0 - Math.abs(chunkContent.length() - targetChunkSize) / (double) targetChunkSize;
        double completenessScore = group.sentences.stream()
                .allMatch(s -> isCompleteSentence(s.text)) ? 1.0 : 0.5;
        double structureScore = group.sentences.size() >= 2 ? 1.0 : 0.7; // Prefer multiple sentences

        return (sizeScore * 0.4 + completenessScore * 0.4 + structureScore * 0.2);
    }

    private double calculateCoherenceScore(SentenceGroup group) {
        // Simple coherence based on sentence length consistency
        if (group.sentences.size() < 2) {
            return 1.0;
        }

        double[] lengths = group.sentences.stream().mapToDouble(s -> s.text.length()).toArray();
        double mean = group.avgSentenceLength;
        double variance = 0.0;

        for (double length : lengths) {
            variance += Math.pow(length - mean, 2);
        }
        variance /= lengths.length;

        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = mean > 0 ? stdDev / mean : 1.0;

        // Lower variation = higher coherence
        return Math.max(0.0, 1.0 - coefficientOfVariation);
    }

    private double calculateDensityScore(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }

        int totalChars = content.length();
        int nonWhitespaceChars = content.replaceAll("\\s", "").length();

        return (double) nonWhitespaceChars / totalChars;
    }

    private boolean isCompleteSentence(String sentence) {
        String trimmed = sentence.trim();
        return trimmed.length() > 10 &&
                (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?"));
    }

    // === Inner Classes ===

    private static class SentenceBoundary {
        final int start;
        final int end;
        final String text;

        SentenceBoundary(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

    private static class SentenceGroup {
        final int startPos;
        final int endPos;
        final List<SentenceBoundary> sentences;
        final double avgSentenceLength;
        final int overlapChars;

        SentenceGroup(int startPos, int endPos, List<SentenceBoundary> sentences,
                      double avgSentenceLength, int overlapChars) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.sentences = new ArrayList<>(sentences);
            this.avgSentenceLength = avgSentenceLength;
            this.overlapChars = overlapChars;
        }
    }
}