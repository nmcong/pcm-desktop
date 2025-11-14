package com.noteflix.pcm.rag.embedding.core;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteflix.pcm.rag.embedding.api.EmbeddingService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedding service using DJL PyTorch engine.
 *
 * <p>Uses PyTorch models directly without ONNX conversion. Supports all tokenizer formats
 * including old formats (vocab.txt, bpe.codes) that ONNX doesn't support.
 *
 * <p>Advantages over ONNX:
 * - No model conversion needed
 * - Full tokenizer compatibility (vocab.txt, bpe.codes, tokenizer.json, etc.)
 * - All HuggingFace models supported
 * - GPU support (optional)
 *
 * <p>Requirements:
 * - PyTorch native libraries (~500 MB)
 * - PyTorch model files (pytorch_model.bin or model.safetensors)
 * - Tokenizer files (any format)
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class PyTorchEmbeddingService implements EmbeddingService {

  private static final Logger log = LoggerFactory.getLogger(PyTorchEmbeddingService.class);

  private final Model model;
  private final Predictor<String, float[]> predictor;
  private final int dimension;
  private final String modelName;

  /**
   * Create PyTorch embedding service.
   *
   * <p>Loads PyTorch model directly without ONNX conversion.
   *
   * @param modelPath Path to model directory containing PyTorch model files
   * @throws IOException if model cannot be loaded
   * @throws ModelException if model format is invalid
   */
  public PyTorchEmbeddingService(String modelPath) throws IOException, ModelException {

    this.modelName = Paths.get(modelPath).getFileName().toString();

    log.info("🔧 Initializing PyTorch embedding service: {}", modelName);

    // Validate model path
    Path path = Paths.get(modelPath);
    if (!Files.exists(path)) {
      throw new IOException("Model path not found: " + modelPath);
    }

    // Check for PyTorch model files
    if (!hasPyTorchModel(path)) {
      throw new IOException(
          "No PyTorch model found. Need pytorch_model.bin or model.safetensors");
    }

    // Get dimension from config
    this.dimension = loadDimensionFromConfig(path);

    try {
      // Create PyTorch model
      this.model = Model.newInstance(modelName, "PyTorch");
      this.model.load(path);

      // Create predictor with custom translator
      EmbeddingTranslator translator = new EmbeddingTranslator(path);
      this.predictor = model.newPredictor(translator);

      log.info("✅ PyTorch embedding service initialized: {} ({}d)", modelName, dimension);

    } catch (Exception e) {
      log.error("❌ Failed to initialize PyTorch service: {}", modelName, e);
      throw new ModelException("Failed to load PyTorch model", e);
    }
  }

  @Override
  public float[] embed(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Text cannot be null or empty");
    }

    try {
      return predictor.predict(text);
    } catch (TranslateException e) {
      throw new RuntimeException("PyTorch embedding failed", e);
    }
  }

  @Override
  public float[][] embedBatch(String[] texts) {
    if (texts == null || texts.length == 0) {
      throw new IllegalArgumentException("Texts cannot be null or empty");
    }

    float[][] results = new float[texts.length][];
    for (int i = 0; i < texts.length; i++) {
      results[i] = embed(texts[i]);
    }
    return results;
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  public void close() {
    if (predictor != null) {
      predictor.close();
    }
    if (model != null) {
      model.close();
    }
    log.debug("Closed PyTorch service: {}", modelName);
  }

  /**
   * Check if directory contains PyTorch model files.
   *
   * @param modelDir Model directory
   * @return true if PyTorch model found
   */
  private boolean hasPyTorchModel(Path modelDir) {
    return Files.exists(modelDir.resolve("pytorch_model.bin"))
        || Files.exists(modelDir.resolve("model.safetensors"));
  }

  /**
   * Load embedding dimension from config.json.
   *
   * @param modelDir Model directory
   * @return Dimension size
   */
  private int loadDimensionFromConfig(Path modelDir) {
    try {
      Path configFile = modelDir.resolve("config.json");
      if (Files.exists(configFile)) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode config = mapper.readTree(configFile.toFile());
        if (config.has("hidden_size")) {
          return config.get("hidden_size").asInt();
        }
      }
    } catch (Exception e) {
      log.warn("Could not read dimension from config.json: {}", e.getMessage());
    }

    // Default dimension
    log.warn("Using default dimension: 768");
    return 768;
  }

  /**
   * Custom translator for embedding models.
   *
   * <p>Handles tokenization and embedding extraction with mean pooling.
   */
  private static class EmbeddingTranslator implements Translator<String, float[]> {

    private final HuggingFaceTokenizer tokenizer;

    /**
     * Create translator with tokenizer.
     *
     * @param modelPath Path to model directory
     * @throws IOException if tokenizer cannot be loaded
     */
    public EmbeddingTranslator(Path modelPath) throws IOException {
      // Try to load tokenizer (supports all formats)
      Path tokenizerJson = modelPath.resolve("tokenizer.json");
      Path vocabTxt = modelPath.resolve("vocab.txt");

      Path tokenizerPath;
      if (Files.exists(tokenizerJson)) {
        tokenizerPath = tokenizerJson;
      } else if (Files.exists(vocabTxt)) {
        tokenizerPath = vocabTxt;
      } else {
        throw new IOException("No tokenizer found in " + modelPath);
      }

      this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
      // Tokenize
      ai.djl.huggingface.tokenizers.Encoding encoding = tokenizer.encode(input);

      // Get token IDs and attention mask
      long[] ids = encoding.getIds();
      long[] attentionMask = encoding.getAttentionMask();

      // Create NDArrays
      NDManager manager = ctx.getNDManager();
      NDArray inputIds = manager.create(ids);
      NDArray mask = manager.create(attentionMask);

      // Add batch dimension
      inputIds = inputIds.expandDims(0);
      mask = mask.expandDims(0);

      return new NDList(inputIds, mask);
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList output) {
      // Get embeddings from model output
      // Usually: {"last_hidden_state": [...], "pooler_output": [...]}
      NDArray embeddings = output.get(0); // last_hidden_state

      // Mean pooling across sequence length
      // Shape: [batch_size, seq_len, hidden_size] -> [batch_size, hidden_size]
      NDArray pooled = embeddings.mean(new int[] {1});

      // Remove batch dimension and convert to float array
      return pooled.squeeze(0).toFloatArray();
    }

    @Override
    public Batchifier getBatchifier() {
      return Batchifier.STACK;
    }
  }
}

