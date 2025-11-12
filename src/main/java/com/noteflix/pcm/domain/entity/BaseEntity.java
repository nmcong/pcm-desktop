package com.noteflix.pcm.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Base entity class with common fields Following DRY principle and providing common functionality
 */
@Data
public abstract class BaseEntity {

  private Long id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /** Called before entity is persisted */
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  /** Called before entity is updated */
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /** Check if entity is new (not yet persisted) */
  public boolean isNew() {
    return id == null;
  }
}
