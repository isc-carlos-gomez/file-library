package com.krloxz.forganizer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import com.krloxz.forganizer.File.Attributes;

public record File(Path path, Optional<Attributes> attributes) {

  public File(final Path path) {
    this(path, Optional.empty());
  }

  public File(final String path) {
    this(Paths.get(path), Optional.empty());
  }

  public File(final Path path, final Attributes attributes) {
    this(path, Optional.of(attributes));
  }

  public record Attributes(String name, long size, LocalDateTime lastModifiedTime) {
    // Empty
  }
}
