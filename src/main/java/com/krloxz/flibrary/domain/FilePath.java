package com.krloxz.flibrary.domain;

public record FilePath(FilePathId id, String value) {

  public FilePath(final String value) {
    this(FilePathId.of(), value);
  }

}
