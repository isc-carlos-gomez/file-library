package com.krloxz.forganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class DirectoryAdder {

  private final FileRepository repository;

  public DirectoryAdder(final FileRepository repository) {
    this.repository = repository;
  }

  public void add(final Path directory) {
    try (Stream<Path> paths = pathsOf(directory)) {
      final AsyncFileAdder fileAdder = new AsyncFileAdder(repository);
      paths.map(File::new)
          .forEach(fileAdder::add);
      fileAdder.complete();
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }

  }

  private Stream<Path> pathsOf(final Path directory) throws IOException {
    return Files.walk(directory);
  }

}
