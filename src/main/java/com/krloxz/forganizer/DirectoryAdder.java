package com.krloxz.forganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class DirectoryAdder {

  private final FilePathRepository repository;

  public DirectoryAdder(final FilePathRepository repository) {
    this.repository = repository;
  }

  public void add(final Path directory) {
    try (Stream<String> paths = pathsOf(directory)) {
      final AsyncFileAdder fileAdder = new AsyncFileAdder(repository);
      paths.map(FilePath::new)
          .forEach(fileAdder::add);
      fileAdder.complete();
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }

  }

  private Stream<String> pathsOf(final Path directory) throws IOException {
    return Files.walk(directory).map(Path::toString);
  }

}
