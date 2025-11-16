package com.noteflix.pcm.rag.chunking.langchain.splitters;

import com.noteflix.pcm.rag.chunking.langchain.LangChainConfig;
import com.noteflix.pcm.rag.chunking.langchain.LangChainDocument;
import com.noteflix.pcm.rag.chunking.langchain.LangChainTextSplitter;
import com.noteflix.pcm.rag.model.DocumentType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LangChain Token Text Splitter implementation.
 *
 * <p>This splitter splits text based on token count rather than character count.
 * It's particularly useful when working with LLMs that have token-based limits.
 *
 * <p>Features:
 * - Token-based splitting (more accurate for LLM usage)
 * - Model-specific tokenization
 * - Encoding-specific handling
 * - Token overlap control
 *
 * <p>Note: This is a simplified implementation. In production, you would integrate
 * with actual tokenization libraries like tiktoken for OpenAI models.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class TokenTextSplitter implements LangChainTextSplitter {

    // Simple token estimation (replace with actual tokenizer in production)
    private static final double AVERAGE_TOKENS_PER_CHAR = 0.25; // Rough estimate
    private int chunkSize; // In tokens
    private int chunkOverlap; // In tokens
    private String modelName;
    private String encodingName;
    private boolean stripWhitespace;

    /**
     * Create Token Text Splitter with configuration.
     *
     * @param config LangChain configuration
     */
    public TokenTextSplitter(LangChainConfig config) {
        this.chunkSize = config.getChunkSize();
        this.chunkOverlap = config.getChunkOverlap();
        this.modelName = config.getTokenConfig().getModelName();
        this.encodingName = config.getTokenConfig().getEncodingName();
        this.stripWhitespace = config.isStripWhitespace();
    }

    /**
     * Create with basic parameters.
     *
     * @param chunkSize    Target chunk size in tokens
     * @param chunkOverlap Overlap between chunks in tokens
     * @param modelName    Model name for tokenization
     */
    public TokenTextSplitter(int chunkSize, int chunkOverlap, String modelName) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.modelName = modelName;
        this.encodingName = getEncodingForModel(modelName);
        this.stripWhitespace = true;
    }

    /**
     * Create splitter for GPT models
     */
    public static TokenTextSplitter forGPT(int chunkSize, int overlap) {
        return new TokenTextSplitter(chunkSize, overlap, "gpt-3.5-turbo");
    }

    /**
     * Create splitter for GPT-4
     */
    public static TokenTextSplitter forGPT4(int chunkSize, int overlap) {
        return new TokenTextSplitter(chunkSize, overlap, "gpt-4");
    }

    /**
     * Create splitter for specific model
     */
    public static TokenTextSplitter forModel(String modelName, int chunkSize, int overlap) {
        return new TokenTextSplitter(chunkSize, overlap, modelName);
    }

    /**
     * Create default token splitter
     */
    public static TokenTextSplitter defaults() {
        return new TokenTextSplitter(500, 50, "gpt-3.5-turbo");
    }

    @Override
    public List<LangChainDocument> splitDocuments(List<LangChainDocument> documents) {
        List<LangChainDocument> allChunks = new ArrayList<>();

        for (LangChainDocument doc : documents) {
            List<String> textChunks = splitText(doc.getPageContent());

            for (int i = 0; i < textChunks.size(); i++) {
                String chunkText = textChunks.get(i);

                // Create new document with chunk content
                LangChainDocument chunkDoc = new LangChainDocument(chunkText);

                // Copy metadata from original document
                if (doc.getMetadata() != null) {
                    chunkDoc.getMetadata().putAll(doc.getMetadata());
                }

                // Add chunk-specific metadata
                chunkDoc.addMetadata("chunk_index", i);
                chunkDoc.addMetadata("chunk_count", textChunks.size());
                chunkDoc.addMetadata("splitter_type", "TokenTextSplitter");
                chunkDoc.addMetadata("model_name", modelName);
                chunkDoc.addMetadata("encoding", encodingName);
                chunkDoc.addMetadata("estimated_tokens", estimateTokenCount(chunkText));

                allChunks.add(chunkDoc);
            }
        }

        return allChunks;
    }

    @Override
    public List<String> splitText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        if (stripWhitespace) {
            text = text.trim();
        }

        // Split text into tokens (simplified - use actual tokenizer in production)
        List<String> tokens = tokenize(text);

        if (tokens.size() <= chunkSize) {
            return List.of(text);
        }

        // Create chunks based on token count
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < tokens.size()) {
            int end = Math.min(start + chunkSize, tokens.size());

            // Extract tokens for this chunk
            List<String> chunkTokens = tokens.subList(start, end);
            String chunkText = detokenize(chunkTokens);

            if (!chunkText.trim().isEmpty()) {
                chunks.add(chunkText);
            }

            // Move to next chunk with overlap
            start += chunkSize - chunkOverlap;

            // Ensure we don't go backwards
            if (start <= 0) {
                start = Math.max(1, chunkSize - chunkOverlap);
            }
        }

        return chunks;
    }

    @Override
    public boolean isSuitableFor(String content, DocumentType documentType) {
        if (content == null || content.length() < 50) {
            return false;
        }

        // Token splitting is suitable for most text types
        // Especially good for LLM-ready content
        int estimatedTokens = estimateTokenCount(content);

        // Suitable if document has reasonable token count
        return estimatedTokens >= 50 && estimatedTokens <= 1000000; // Reasonable range
    }

    @Override
    public double estimateQuality(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }

        int estimatedTokens = estimateTokenCount(content);

        // Quality factors for token-based splitting:

        // 1. Token count appropriateness
        double tokenCountScore = Math.min(1.0, (double) estimatedTokens / (chunkSize * 10));

        // 2. Token density (fewer tokens per character = better structure)
        double tokenDensity = (double) estimatedTokens / content.length();
        double densityScore = Math.max(0.0, 1.0 - Math.abs(tokenDensity - AVERAGE_TOKENS_PER_CHAR) * 4);

        // 3. Content structure (presence of words, sentences)
        double structureScore = calculateStructureScore(content);

        // 4. Model compatibility
        double modelScore = getModelCompatibilityScore(content);

        return (tokenCountScore * 0.3 + densityScore * 0.2 + structureScore * 0.3 + modelScore * 0.2);
    }

    // === Private Helper Methods ===

    @Override
    public void configure(Map<String, Object> parameters) {
        if (parameters.containsKey("chunk_size")) {
            this.chunkSize = (Integer) parameters.get("chunk_size");
        }
        if (parameters.containsKey("chunk_overlap")) {
            this.chunkOverlap = (Integer) parameters.get("chunk_overlap");
        }
        if (parameters.containsKey("model_name")) {
            this.modelName = (String) parameters.get("model_name");
            this.encodingName = getEncodingForModel(modelName);
        }
        if (parameters.containsKey("encoding_name")) {
            this.encodingName = (String) parameters.get("encoding_name");
        }
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public int getChunkOverlap() {
        return chunkOverlap;
    }

    @Override
    public String getSplitterName() {
        return "TokenTextSplitter";
    }

    /**
     * Tokenize text into list of tokens.
     * <p>
     * Note: This is a simplified implementation. In production, use proper
     * tokenization libraries like tiktoken for OpenAI models.
     *
     * @param text Text to tokenize
     * @return List of tokens
     */
    private List<String> tokenize(String text) {
        // Simplified tokenization - split by whitespace and punctuation
        // In production, use model-specific tokenizers

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(c)); // Include whitespace as tokens
            } else if (isPunctuation(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(c)); // Punctuation as separate tokens
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    /**
     * Convert tokens back to text.
     *
     * @param tokens List of tokens
     * @return Reconstructed text
     */
    private String detokenize(List<String> tokens) {
        StringBuilder text = new StringBuilder();

        for (String token : tokens) {
            text.append(token);
        }

        return text.toString();
    }

    /**
     * Estimate token count for text.
     *
     * @param text Text to analyze
     * @return Estimated token count
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Simple estimation - in production, use actual tokenizer
        return (int) Math.ceil(text.length() * AVERAGE_TOKENS_PER_CHAR);
    }

    // === Factory Methods ===

    /**
     * Get encoding name for model.
     *
     * @param modelName Model name
     * @return Encoding name
     */
    private String getEncodingForModel(String modelName) {
        if (modelName == null) {
            return "cl100k_base";
        }

        // Map common models to their encodings
        return switch (modelName.toLowerCase()) {
            case "gpt-4", "gpt-4-turbo", "gpt-3.5-turbo" -> "cl100k_base";
            case "text-davinci-003", "text-davinci-002" -> "p50k_base";
            case "gpt-2" -> "gpt2";
            default -> "cl100k_base";
        };
    }

    /**
     * Check if character is punctuation.
     *
     * @param c Character to check
     * @return true if punctuation
     */
    private boolean isPunctuation(char c) {
        return ".,!?;:()[]{}\"'-".indexOf(c) >= 0;
    }

    /**
     * Calculate structure score for content.
     *
     * @param content Content to analyze
     * @return Structure score (0.0-1.0)
     */
    private double calculateStructureScore(String content) {
        double score = 0.5; // Base score

        // Check for various structural elements
        if (content.matches(".*[.!?].*")) score += 0.1; // Has sentences
        if (content.contains("\n")) score += 0.1; // Has line breaks
        if (content.matches(".*[A-Z].*")) score += 0.1; // Has uppercase letters
        if (content.matches(".*\\d.*")) score += 0.1; // Has numbers
        if (content.length() > 200) score += 0.1; // Reasonable length

        return Math.min(1.0, score);
    }

    /**
     * Get model compatibility score.
     *
     * @param content Content to analyze
     * @return Compatibility score (0.0-1.0)
     */
    private double getModelCompatibilityScore(String content) {
        // Simple compatibility based on content characteristics
        double score = 0.7; // Base score

        // Check for model-specific characteristics
        if (modelName.toLowerCase().contains("gpt")) {
            // GPT models handle various content types well
            score += 0.2;
        }

        // Penalty for very short content
        if (content.length() < 100) {
            score -= 0.2;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }
}