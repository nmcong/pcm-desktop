package com.noteflix.pcm.ui.components.text;

/** Enum defining the viewing/editing modes for the Universal Text Component. */
public enum ViewMode {
  /** Read-only view mode - displays rendered content */
  VIEW("View", "👁️"),

  /** Edit-only mode - shows text editor */
  EDIT("Edit", "✏️"),

  /** Split view mode - editor and preview side by side */
  SPLIT("Split", "⚡");

  private final String displayName;
  private final String icon;

  ViewMode(String displayName, String icon) {
    this.displayName = displayName;
    this.icon = icon;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getIcon() {
    return icon;
  }

  /** Get the next view mode in sequence (for toggle functionality) */
  public ViewMode next() {
    ViewMode[] values = ViewMode.values();
    int nextIndex = (this.ordinal() + 1) % values.length;
    return values[nextIndex];
  }

  /** Check if this mode includes viewing capability */
  public boolean canView() {
    return this == VIEW || this == SPLIT;
  }

  /** Check if this mode includes editing capability */
  public boolean canEdit() {
    return this == EDIT || this == SPLIT;
  }
}
