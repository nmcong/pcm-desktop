package com.noteflix.pcm.rag.embedding.model;

/**
 * Supported languages for multi-model embedding architecture.
 *
 * <p>Each language can have its own specialized embedding model optimized for that language's
 * characteristics. The system will automatically fallback to a universal model if the
 * language-specific model is unavailable.
 *
 * @author PCM Team
 * @version 1.0.0
 * @since 2024-11
 */
public enum Language {
  /** Vietnamese language */
  VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),

  /** English language */
  ENGLISH("en", "English", "English"),

  /** Auto-detect language (not implemented yet) */
  AUTO("auto", "Auto-detect", "Tự động"),

  /** Unknown or unsupported language - will use fallback model */
  UNKNOWN("unknown", "Unknown", "Không xác định");

  private final String code;
  private final String name;
  private final String nativeName;

  Language(String code, String name, String nativeName) {
    this.code = code;
    this.name = name;
    this.nativeName = nativeName;
  }

  /**
   * Get language code (ISO 639-1).
   *
   * @return Language code (e.g., "vi", "en")
   */
  public String getCode() {
    return code;
  }

  /**
   * Get English name.
   *
   * @return English name (e.g., "Vietnamese")
   */
  public String getName() {
    return name;
  }

  /**
   * Get native name.
   *
   * @return Native name (e.g., "Tiếng Việt")
   */
  public String getNativeName() {
    return nativeName;
  }

  /**
   * Get Language from code.
   *
   * @param code Language code
   * @return Language enum
   */
  public static Language fromCode(String code) {
    if (code == null) {
      return UNKNOWN;
    }

    for (Language lang : values()) {
      if (lang.code.equalsIgnoreCase(code)) {
        return lang;
      }
    }

    return UNKNOWN;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", name, code);
  }
}

