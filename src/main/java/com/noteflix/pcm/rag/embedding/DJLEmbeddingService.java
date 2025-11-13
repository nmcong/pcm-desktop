package com.noteflix.pcm.rag.embedding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * DJL (Deep Java Library) based embedding service.
 *
 * <p>Uses sentence-transformers models (ONNX format) for offline embeddings.
 *
 * <p>Setup: 1. Download DJL libraries (see setup script) 2. Download model (e.g., all-MiniLM-L6-v2)
 * 3. Use this service
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
 * @version 1.0.0
 */
@Slf4j
public class DJLEmbeddingService implements EmbeddingService {

  private final String modelPath;
  private final int dimension;
  private final String modelName;

  // NOTE: Actual DJL objects would be here when DJL libs are available
  // private ZooModel<String, float[]> model;
  // private Predictor<String, float[]> predictor;

  /**
   * Create DJL embedding service.
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

    // Determine dimension from model name
    this.dimension = getDimensionFromModelName(modelName);

    // Initialize DJL (when libs available)
    initializeDJL();

    log.info("DJL Embedding service initialized: {} ({}d)", modelName, dimension);
  }

  @Override
  public float[] embed(String text) {
    // TODO: When DJL libs are added, use this:
    /*
    try {
        return predictor.predict(text);
    } catch (TranslateException e) {
        throw new RuntimeException("Embedding failed", e);
    }
    */

    // Placeholder: Return error
    throw new UnsupportedOperationException(
        "DJL libraries not yet added to classpath.\n"
            + "To enable DJL embeddings:\n"
            + "1. Run: ./scripts/setup-embeddings-djl.sh\n"
            + "2. Rebuild: ./scripts/build.sh\n"
            + "3. Use this service\n\n"
            + "Required JARs:\n"
            + "  - ai.djl:api:0.25.0\n"
            + "  - ai.djl.onnx:onnx-engine:0.25.0\n"
            + "  - ai.djl.huggingface:tokenizers:0.25.0\n\n"
            + "Alternative: Use ONNXEmbeddingService or SimpleEmbeddingService");
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
    // TODO: When DJL available
    /*
    if (predictor != null) {
        predictor.close();
    }
    if (model != null) {
        model.close();
    }
    */
    log.info("DJL embedding service closed");
  }

  // ========== Private Methods ==========

  private void initializeDJL() {
    // TODO: When DJL libs are available, implement:
    /*
    Criteria<String, float[]> criteria = Criteria.builder()
        .setTypes(String.class, float[].class)
        .optModelPath(Path.of(modelPath))
        .optEngine("OnnxRuntime")
        .optTranslator(new SentenceTransformer())
        .build();

    this.model = criteria.loadModel();
    this.predictor = model.newPredictor();
    */

    log.debug("DJL initialization placeholder (libs not yet loaded)");
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

  /** Create default service (all-MiniLM-L6-v2). */
  public static DJLEmbeddingService createDefault() throws IOException {
    return new DJLEmbeddingService("data/models/all-MiniLM-L6-v2");
  }
}
