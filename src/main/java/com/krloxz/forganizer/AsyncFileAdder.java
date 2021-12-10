package com.krloxz.forganizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class AsyncFileAdder {

  private static final int MAX_BATCH_SIZE = 10_000;
  private final FilePathRepository repository;
  private final List<CompletableFuture<Void>> batchesToComplete;
  private List<FilePath> batch;

  AsyncFileAdder(final FilePathRepository repository) {
    this.repository = repository;
    this.batchesToComplete = new ArrayList<>();
    this.batch = new ArrayList<>(MAX_BATCH_SIZE);
  }

  void add(final FilePath file) {
    this.batch.add(file);
    if (this.batch.size() == MAX_BATCH_SIZE) {
      this.batchesToComplete.add(CompletableFuture.runAsync(() -> this.repository.saveAll(batch)));
      this.batch = new ArrayList<>();
    }
  }

  void complete() {
    this.batchesToComplete.add(CompletableFuture.runAsync(() -> this.repository.saveAll(batch)));
    CompletableFuture.allOf(this.batchesToComplete.toArray(CompletableFuture[]::new)).join();
  }

}
