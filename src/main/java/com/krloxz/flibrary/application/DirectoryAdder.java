package com.krloxz.flibrary.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.krloxz.flibrary.domain.FilePath;
import com.krloxz.flibrary.domain.FilePathRepository;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class DirectoryAdder {

  private final FilePathRepository repository;

  public DirectoryAdder(final FilePathRepository repository) {
    this.repository = repository;
  }

  public void add(final Path directory) {
    pathsOf(directory)
        .map(Path::toString)
        .map(FilePath::new)
        .buffer(100)
        .flatMap(paths -> this.repository.saveAll(paths).subscribeOn(Schedulers.boundedElastic()))
        .blockLast();
  }

  private Flux<Path> pathsOf(final Path directory) {
    try {
      return Flux.fromStream(Files.walk(directory));
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
