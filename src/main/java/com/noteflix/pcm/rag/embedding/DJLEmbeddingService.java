package com.noteflix.pcm.rag.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * DJL (Deep Java Library) based embedding service.
 *
 * <p>Uses sentence-transformers models (ONNX format) for offline embeddings with DJL ONNX Runtime.
 *
 * <p>Setup: 1. Download DJL libraries: ./scripts/setup-embeddings-djl.sh 2. Download model (e.g.,
 * all-MiniLM-L6-v2) 3. Use this service
 *
 * <p>Example:
 *
 * <pre>
 * EmbeddingService embeddings = new DJLEmbeddingService(
 *     "data/models/all-MiniLM-L6-v2"
 * );
 *
 * float[] vector = embeddings.embed("How to validate customers?");
 * </pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public class DJLEmbeddingService implements EmbeddingService {

  private final String modelPath;
  private final int dimension;
  private final String modelName;

  // DJL ONNX Runtime components
  private OrtEnvironment env;
  private OrtSession session;
  private HuggingFaceTokenizer tokenizer;
  private final int maxLength = 512;

  /**
   * Create DJL embedding service with ONNX Runtime backend.
   *
   * @param modelPath Path to model directory
   * @throws IOException if model cannot be loaded
   */
  public DJLEmbeddingService(String modelPath) throws IOException {
    this.modelPath = modelPath;
    this.modelName = Paths.get(modelPath).getFileName().toString();

    // Validate model path
    Path path = Paths.get(modelPath);
    if (!Files.exists(path)) {
      throw new IOException(
          "Model not found: "
              + modelPath
              + "\n"
              + "Please run setup script: ./scripts/setup-embeddings-djl.sh\n"
              + "Or download model from: https://huggingface.co/sentence-transformers/"
              + modelName);
    }

    // Check for required files
    checkRequiredFiles(path);

    // Determine dimension from model name or config
    this.dimension = loadDimensionFromConfig(path);

    // Initialize DJL ONNX Runtime
    try {
      initializeDJL();
      log.info("✅ DJL Embedding service initialized: {} ({}d)", modelName, dimension);
    } catch (Exception e) {
      log.error("❌ Failed to initialize DJL: {}", e.getMessage());
      throw new IOException("Failed to initialize DJL ONNX Runtime: " + e.getMessage(), e);
    }
  }

  @Override
  public float[] embed(String text) {
    OnnxTensor inputIdsTensor = null;
    OnnxTensor attentionMaskTensor = null;
    OnnxTensor tokenTypeIdsTensor = null;
    OrtSession.Result result = null;

    try {
      if (text == null || text.trim().isEmpty()) {
        text = "[EMPTY]"; // Avoid empty inputs
      }

      // Tokenize input using proper HuggingFace tokenizer
      Encoding encoding = tokenizer.encode(text);
      long[] inputIds = encoding.getIds();
      long[] attentionMask = encoding.getAttentionMask();
      long[] tokenTypeIds = encoding.getTypeIds();
      
      // Pad or truncate to maxLength
      inputIds = padOrTruncate(inputIds, maxLength);
      attentionMask = padOrTruncate(attentionMask, maxLength);
      tokenTypeIds = padOrTruncate(tokenTypeIds, maxLength);

      // Convert to 2D arrays for ONNX
      long[][] inputIds2D = new long[][] {inputIds};
      long[][] attentionMask2D = new long[][] {attentionMask};
      long[][] tokenTypeIds2D = new long[][] {tokenTypeIds};

      // Create ONNX tensors
      inputIdsTensor = OnnxTensor.createTensor(env, inputIds2D);
      attentionMaskTensor = OnnxTensor.createTensor(env, attentionMask2D);
      tokenTypeIdsTensor = OnnxTensor.createTensor(env, tokenTypeIds2D);

      // Prepare inputs map
      Map<String, OnnxTensor> inputs = new HashMap<>();
      inputs.put("input_ids", inputIdsTensor);
      inputs.put("attention_mask", attentionMaskTensor);
      inputs.put("token_type_ids", tokenTypeIdsTensor);

      // Run inference
      result = session.run(inputs);

      // Get output embeddings (last_hidden_state)
      float[][][] outputTensor =
          (float[][][]) result.get(0).getValue(); // Shape: [batch_size, seq_len, hidden_size]

      // Mean pooling
      float[] embedding = meanPooling(outputTensor[0], attentionMask);

      // Normalize
      normalize(embedding);

      return embedding;

    } catch (OrtException e) {
      throw new RuntimeException("ONNX Runtime inference failed: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RuntimeException("Embedding generation failed: " + e.getMessage(), e);
    } finally {
      // Cleanup tensors
      if (inputIdsTensor != null) {
        inputIdsTensor.close();
      }
      if (attentionMaskTensor != null) {
        attentionMaskTensor.close();
      }
      if (tokenTypeIdsTensor != null) {
        tokenTypeIdsTensor.close();
      }
      if (result != null) {
        result.close();
      }
    }
  }

  @Override
  public float[][] embedBatch(String[] texts) {
    float[][] embeddings = new float[texts.length][];

    for (int i = 0; i < texts.length; i++) {
      embeddings[i] = embed(texts[i]);
    }

    return embeddings;
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  /** Close resources and cleanup. */
  public void close() {
    try {
      if (tokenizer != null) {
        tokenizer.close();
      }
      if (session != null) {
        session.close();
      }
      if (env != null) {
        env.close();
      }
      log.info("✅ DJL embedding service closed");
    } catch (OrtException e) {
      log.error("Error closing ONNX Runtime session: {}", e.getMessage());
    }
  }

  // ========== Private Methods ==========

  private void initializeDJL() throws OrtException, IOException {
    log.info("🔧 Initializing DJL ONNX Runtime...");

    // Initialize ONNX Runtime environment
    env = OrtEnvironment.getEnvironment();

    // Load ONNX model
    Path modelFile = Paths.get(modelPath, "model.onnx");
    if (!Files.exists(modelFile)) {
      throw new IOException("Model file not found: " + modelFile);
    }

    OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
    session = env.createSession(modelFile.toString(), opts);

    log.info("✅ ONNX model loaded: {}", modelFile);

    // Load proper HuggingFace tokenizer
    Path tokenizerFile = Paths.get(modelPath, "tokenizer.json");
    if (!Files.exists(tokenizerFile)) {
      throw new IOException("Tokenizer file not found: " + tokenizerFile);
    }

    // Use DJL HuggingFace tokenizer for production-grade tokenization
    tokenizer = HuggingFaceTokenizer.newInstance(tokenizerFile);
    log.info("✅ HuggingFace tokenizer loaded: {}", tokenizerFile);
  }

  private void checkRequiredFiles(Path modelDir) throws IOException {
    String[] requiredFiles = {"model.onnx", "tokenizer.json"};

    for (String file : requiredFiles) {
      Path filePath = modelDir.resolve(file);
      if (!Files.exists(filePath)) {
        throw new IOException(
            "Missing required file: "
                + file
                + "\n"
                + "Model directory: "
                + modelDir
                + "\n"
                + "Please download complete model from HuggingFace");
      }
    }
  }

  private int loadDimensionFromConfig(Path modelDir) {
    // Try to read from config.json
    try {
      Path configFile = modelDir.resolve("config.json");
      if (Files.exists(configFile)) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode config = mapper.readTree(configFile.toFile());
        if (config.has("hidden_size")) {
          return config.get("hidden_size").asInt();
        }
      }
    } catch (IOException e) {
      log.warn("Failed to read config.json, using model name heuristics");
    }

    // Fallback to model name heuristics
    String name = modelDir.getFileName().toString();
    if (name.contains("MiniLM")) {
      return 384;
    } else if (name.contains("mpnet")) {
      return 768;
    } else if (name.contains("e5-small")) {
      return 384;
    } else if (name.contains("e5-base") || name.contains("e5-large")) {
      return 768;
    } else if (name.contains("bge-small")) {
      return 384;
    } else if (name.contains("bge-base") || name.contains("bge-large")) {
      return 768;
    }

    // Default
    log.warn("Unknown model dimension, defaulting to 384");
    return 384;
  }

  private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
    // Mean pooling: average all token embeddings weighted by attention mask
    int seqLen = tokenEmbeddings.length;
    int hiddenSize = tokenEmbeddings[0].length;

    float[] pooled = new float[hiddenSize];
    float sumMask = 0;

    for (int i = 0; i < seqLen; i++) {
      float mask = attentionMask[i];
      sumMask += mask;
      for (int j = 0; j < hiddenSize; j++) {
        pooled[j] += tokenEmbeddings[i][j] * mask;
      }
    }

    // Average
    if (sumMask > 0) {
      for (int j = 0; j < hiddenSize; j++) {
        pooled[j] /= sumMask;
      }
    }

    return pooled;
  }

  private void normalize(float[] embedding) {
    // L2 normalization
    double norm = 0;
    for (float v : embedding) {
      norm += v * v;
    }
    norm = Math.sqrt(norm);

    if (norm > 0) {
      for (int i = 0; i < embedding.length; i++) {
        embedding[i] /= norm;
      }
    }
  }

  /** Pad or truncate array to target length. */
  private long[] padOrTruncate(long[] array, int targetLength) {
    if (array.length == targetLength) {
      return array;
    } else if (array.length < targetLength) {
      // Pad with zeros
      return Arrays.copyOf(array, targetLength);
    } else {
      // Truncate
      return Arrays.copyOfRange(array, 0, targetLength);
    }
  }

  /** Create default service (all-MiniLM-L6-v2). */
  public static DJLEmbeddingService createDefault() throws IOException {
    return new DJLEmbeddingService("data/models/all-MiniLM-L6-v2");
  }
}
