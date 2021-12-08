package com.krloxz.forganizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class AsyncFileAdder {

  private static final int MAX_BATCH_SIZE = 10_000;
  private final FileRepository repository;
  private final List<CompletableFuture<Void>> batchesToComplete;
  private List<File> batch;

  AsyncFileAdder(final FileRepository repository) {
    this.repository = repository;
    this.batchesToComplete = new ArrayList<>();
    this.batch = new ArrayList<>(MAX_BATCH_SIZE);
  }

  void add(final File file) {
    this.batch.add(file);
    if (this.batch.size() == MAX_BATCH_SIZE) {
      this.batchesToComplete.add(this.repository.saveAll(batch));
      this.batch = new ArrayList<>();
    }
  }

  void complete() {
    this.batchesToComplete.add(this.repository.saveAll(batch));
    CompletableFuture.allOf(this.batchesToComplete.toArray(CompletableFuture[]::new)).join();
  }

}
