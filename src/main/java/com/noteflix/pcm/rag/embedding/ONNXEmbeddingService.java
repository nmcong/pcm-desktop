package com.noteflix.pcm.rag.embedding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * ONNX Runtime based embedding service.
 *
 * <p>Uses ONNX models directly with ONNX Runtime Java API. More control and potentially faster than
 * DJL.
 *
 * <p>Setup: 1. Download ONNX Runtime JAR 2. Download tokenizer library 3. Download model 4. Use
 * this service
 *
 * <p>Example:
 *
 * <pre>
 * EmbeddingService embeddings = new ONNXEmbeddingService(
 *     "data/models/all-MiniLM-L6-v2"
 * );
 *
 * float[] vector = embeddings.embed("How to validate customers?");
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ONNXEmbeddingService implements EmbeddingService {

  private final String modelPath;
  private final int dimension;
  private final String modelName;

  // NOTE: Actual ONNX objects would be here when ONNX Runtime is available
  // private OrtEnvironment env;
  // private OrtSession session;
  // private Tokenizer tokenizer;

  /**
   * Create ONNX embedding service.
   *
   * @param modelPath Path to model directory
   * @throws IOException if model cannot be loaded
   */
  public ONNXEmbeddingService(String modelPath) throws IOException {
    this.modelPath = modelPath;
    this.modelName = Paths.get(modelPath).getFileName().toString();

    // Validate model path
    Path path = Paths.get(modelPath);
    if (!Files.exists(path)) {
      throw new IOException(
          "Model not found: "
              + modelPath
              + "\n"
              + "Please run setup script: ./scripts/setup-embeddings-onnx.sh\n"
              + "Or download model from: https://huggingface.co/sentence-transformers/"
              + modelName);
    }

    // Check for required files
    checkRequiredFiles(path);

    // Determine dimension from model name
    this.dimension = getDimensionFromModelName(modelName);

    // Initialize ONNX Runtime (when libs available)
    initializeONNX();

    log.info("ONNX Embedding service initialized: {} ({}d)", modelName, dimension);
  }

  @Override
  public float[] embed(String text) {
    // TODO: When ONNX Runtime libs are added, use this:
    /*
    try {
        // 1. Tokenize
        TokenizerOutput tokens = tokenizer.encode(text);

        // 2. Create ONNX inputs
        Map<String, OnnxTensor> inputs = new HashMap<>();

        long[] inputIds = tokens.getInputIds();
        long[] attentionMask = tokens.getAttentionMask();

        long[][] inputIdsArray = new long[][] { inputIds };
        long[][] attentionMaskArray = new long[][] { attentionMask };

        inputs.put("input_ids", OnnxTensor.createTensor(env, inputIdsArray));
        inputs.put("attention_mask", OnnxTensor.createTensor(env, attentionMaskArray));

        // 3. Run inference
        OrtSession.Result result = session.run(inputs);

        // 4. Get embeddings
        float[][] embeddings = (float[][]) result.get(0).getValue();

        // 5. Mean pooling
        float[] pooled = meanPooling(embeddings[0], attentionMask);

        // 6. Normalize
        normalize(pooled);

        return pooled;

    } catch (Exception e) {
        throw new RuntimeException("Embedding failed", e);
    }
    */

    // Placeholder: Return error
    throw new UnsupportedOperationException(
        "ONNX Runtime libraries not yet added to classpath.\n"
            + "To enable ONNX embeddings:\n"
            + "1. Run: ./scripts/setup-embeddings-onnx.sh\n"
            + "2. Rebuild: ./scripts/build.sh\n"
            + "3. Use this service\n\n"
            + "Required JARs:\n"
            + "  - com.microsoft.onnxruntime:onnxruntime:1.16.3\n"
            + "  - ai.djl.huggingface:tokenizers:0.25.0 (for tokenization)\n\n"
            + "Alternative: Use DJLEmbeddingService or SimpleEmbeddingService");
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

  /** Close resources. */
  public void close() {
    // TODO: When ONNX available
    /*
    if (session != null) {
        session.close();
    }
    if (env != null) {
        env.close();
    }
    */
    log.info("ONNX embedding service closed");
  }

  // ========== Private Methods ==========

  private void initializeONNX() {
    // TODO: When ONNX Runtime libs are available, implement:
    /*
    this.env = OrtEnvironment.getEnvironment();

    OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
    this.session = env.createSession(modelPath + "/model.onnx", opts);

    this.tokenizer = new Tokenizer(modelPath + "/tokenizer.json");
    */

    log.debug("ONNX initialization placeholder (libs not yet loaded)");
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

  private int getDimensionFromModelName(String name) {
    // Common models and their dimensions
    if (name.contains("MiniLM")) {
      return 384;
    } else if (name.contains("mpnet")) {
      return 768;
    } else if (name.contains("e5-small")) {
      return 384;
    } else if (name.contains("e5-base")) {
      return 768;
    }

    // Default
    return 384;
  }

  /** Mean pooling with attention mask. */
  private float[] meanPooling(float[] embeddings, long[] mask) {
    float[] result = new float[dimension];
    int validTokens = 0;

    for (int i = 0; i < mask.length; i++) {
      if (mask[i] == 1) {
        for (int j = 0; j < dimension; j++) {
          result[j] += embeddings[i * dimension + j];
        }
        validTokens++;
      }
    }

    for (int j = 0; j < dimension; j++) {
      result[j] /= validTokens;
    }

    return result;
  }

  /** L2 normalization. */
  private void normalize(float[] vector) {
    float norm = 0;

    for (float v : vector) {
      norm += v * v;
    }

    norm = (float) Math.sqrt(norm);

    if (norm > 0) {
      for (int i = 0; i < vector.length; i++) {
        vector[i] /= norm;
      }
    }
  }

  /** Create default service (all-MiniLM-L6-v2). */
  public static ONNXEmbeddingService createDefault() throws IOException {
    return new ONNXEmbeddingService("data/models/all-MiniLM-L6-v2");
  }
}
