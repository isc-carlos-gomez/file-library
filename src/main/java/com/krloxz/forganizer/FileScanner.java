package com.krloxz.forganizer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class FileScanner {

  private final FileRepository repository;
  private final AttributeReader attributeReader;

  public FileScanner(final FileRepository repository, final AttributeReader attributeReader) {
    this.repository = repository;
    this.attributeReader = attributeReader;
  }

  public Flux<Integer> scan() {
    final Scheduler scheduler = Schedulers.boundedElastic();
    final AtomicInteger progress = new AtomicInteger();
    return this.repository.findAllWithNoAttributes()
        .flatMap(file -> this.attributeReader.read(file).subscribeOn(scheduler), 100)
        .buffer(100)
        .flatMap(files -> saveAll(files).subscribeOn(scheduler))
        .map(savedFiles -> progress.addAndGet(savedFiles.size()))
        .doOnCancel(() -> System.out.println("Flux CANCELLED"));
  }

  private Mono<String> toFileWithAttributes(final File file) {
    return Mono.fromSupplier(
        () -> {
          ioDelay(1000 /* + new Random().nextInt(1000) */);
          debugThread("Finding attributes for " + file);
          return file + "WithAttributes";
        });
  }

  private void ioDelay(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (final InterruptedException e) {
      throw new IllegalArgumentException("Delay was interrupted", e);
    }
  }

  private Mono<List<String>> saveAll(final List<File> files) {
    return Mono.fromSupplier(
        () -> {
          ioDelay(1000);
          debugThread("Saving files: " + files);
          return files.stream().map(file -> file + "Saved").toList();
        });
  }

  private void debugThread(final Object message) {
    System.out.printf("Thread %s - %s\n", Thread.currentThread().getName(), message);
  }

}
