package com.noteflix.pcm.rag.embedding.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Simple tokenizer for models using vocab.txt format (e.g. PhoBERT).
 *
 * <p>This is a fallback tokenizer when HuggingFace fast tokenizer is not available.
 * Implements basic WordPiece tokenization.
 *
 * <p>Limitations:
 * - Basic tokenization only (no BPE)
 * - May not match Python tokenizer exactly
 * - For production, use models with fast tokenizer (tokenizer.json)
 *
 * @author PCM Team
 */
public class VocabTokenizer {

  private final Map<String, Integer> vocab;
  private final List<String> idToToken;
  private final int maxInputLength;

  // Special tokens
  private final int clsTokenId;
  private final int sepTokenId;
  private final int padTokenId;
  private final int unkTokenId;

  /**
   * Load tokenizer from vocab.txt file.
   *
   * @param vocabFile Path to vocab.txt
   * @param maxLength Maximum sequence length
   * @throws IOException if vocab file cannot be read
   */
  public VocabTokenizer(Path vocabFile, int maxLength) throws IOException {
    this.maxInputLength = maxLength;
    this.vocab = new HashMap<>();
    this.idToToken = new ArrayList<>();

    // Load vocabulary
    try (BufferedReader reader = Files.newBufferedReader(vocabFile)) {
      String line;
      int id = 0;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          vocab.put(line, id);
          idToToken.add(line);
          id++;
        }
      }
    }

    // Find special token IDs
    this.clsTokenId = vocab.getOrDefault("[CLS]", 0);
    this.sepTokenId = vocab.getOrDefault("[SEP]", 2);
    this.padTokenId = vocab.getOrDefault("[PAD]", 1);
    this.unkTokenId = vocab.getOrDefault("[UNK]", 3);
  }

  /**
   * Tokenize text into token IDs.
   *
   * @param text Input text
   * @return Tokenization result
   */
  public TokenizationResult tokenize(String text) {
    List<Integer> tokenIds = new ArrayList<>();
    List<Integer> attentionMask = new ArrayList<>();

    // Add [CLS] token
    tokenIds.add(clsTokenId);
    attentionMask.add(1);

    // Basic tokenization
    String[] words = text.toLowerCase().split("\\s+");

    for (String word : words) {
      if (word.isEmpty()) continue;

      // Look up word in vocabulary
      Integer tokenId = vocab.get(word);

      if (tokenId != null) {
        // Word found in vocab
        tokenIds.add(tokenId);
        attentionMask.add(1);
      } else {
        // Word not found - try WordPiece tokenization
        List<Integer> subwordIds = tokenizeWord(word);
        tokenIds.addAll(subwordIds);
        for (int i = 0; i < subwordIds.size(); i++) {
          attentionMask.add(1);
        }
      }

      // Check max length
      if (tokenIds.size() >= maxInputLength - 1) {
        break;
      }
    }

    // Add [SEP] token
    tokenIds.add(sepTokenId);
    attentionMask.add(1);

    // Convert to arrays
    long[] inputIds = tokenIds.stream().mapToLong(Integer::longValue).toArray();
    long[] attentionMaskArray = attentionMask.stream().mapToLong(Integer::longValue).toArray();

    return new TokenizationResult(inputIds, attentionMaskArray);
  }

  /**
   * Tokenize a single word using WordPiece algorithm.
   *
   * @param word Word to tokenize
   * @return List of subword token IDs
   */
  private List<Integer> tokenizeWord(String word) {
    List<Integer> subwordIds = new ArrayList<>();

    int start = 0;
    while (start < word.length()) {
      int end = word.length();
      boolean found = false;

      // Try to find longest matching subword
      while (start < end) {
        String substr = word.substring(start, end);

        // Add ## prefix for non-initial subwords
        if (start > 0) {
          substr = "##" + substr;
        }

        Integer tokenId = vocab.get(substr);
        if (tokenId != null) {
          subwordIds.add(tokenId);
          start = end;
          found = true;
          break;
        }

        end--;
      }

      if (!found) {
        // No matching subword found - use [UNK]
        subwordIds.add(unkTokenId);
        start++;
      }
    }

    return subwordIds;
  }

  /**
   * Get vocabulary size.
   *
   * @return Number of tokens in vocabulary
   */
  public int getVocabSize() {
    return vocab.size();
  }

  /**
   * Convert token ID to token string.
   *
   * @param tokenId Token ID
   * @return Token string
   */
  public String idToToken(int tokenId) {
    if (tokenId >= 0 && tokenId < idToToken.size()) {
      return idToToken.get(tokenId);
    }
    return "[UNK]";
  }

  /** Tokenization result containing input IDs and attention mask */
  public static class TokenizationResult {
    private final long[] inputIds;
    private final long[] attentionMask;

    public TokenizationResult(long[] inputIds, long[] attentionMask) {
      this.inputIds = inputIds;
      this.attentionMask = attentionMask;
    }

    public long[] getInputIds() {
      return inputIds;
    }

    public long[] getAttentionMask() {
      return attentionMask;
    }

    public int getLength() {
      return inputIds.length;
    }
  }
}

