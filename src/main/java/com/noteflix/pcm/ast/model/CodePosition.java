package com.noteflix.pcm.ast.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodePosition {

  private int startLine;
  private int endLine;
  private int startColumn;
  private int endColumn;

  // Character positions in file
  private int startOffset;
  private int endOffset;

  // Context information
  private String fileName;
  private String packageName;
  private String className;

  public String getLocationString() {
    return String.format("%s:%d:%d", fileName, startLine, startColumn);
  }

  public String getRangeString() {
    return String.format("%s:%d:%d-%d:%d", fileName, startLine, startColumn, endLine, endColumn);
  }

  public int getLengthInLines() {
    return endLine - startLine + 1;
  }

  public int getLengthInCharacters() {
    return endOffset - startOffset;
  }

  public boolean contains(CodePosition other) {
    return startLine <= other.startLine
        && endLine >= other.endLine
        && (startLine < other.startLine || startColumn <= other.startColumn)
        && (endLine > other.endLine || endColumn >= other.endColumn);
  }

  public boolean overlaps(CodePosition other) {
    return !(endLine < other.startLine
        || startLine > other.endLine
        || (endLine == other.startLine && endColumn < other.startColumn)
        || (startLine == other.endLine && startColumn > other.endColumn));
  }
}
