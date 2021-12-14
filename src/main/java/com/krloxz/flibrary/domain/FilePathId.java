package com.krloxz.flibrary.domain;

import java.util.UUID;

public record FilePathId(UUID value) {

  public static FilePathId of(final UUID value) {
    return new FilePathId(value);
  }

  public static FilePathId of() {
    return of(UUID.randomUUID());
  }

}
