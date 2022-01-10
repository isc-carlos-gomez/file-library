package com.krloxz.flibrary.presentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.jooq.DSLContext;
import org.reactivestreams.Subscription;

import com.krloxz.forganaizer.infra.jooq.library.tables.records.FilePathsRecord;

import javafx.concurrent.Task;
import reactor.core.publisher.Flux;

public class AddDirectoryTask extends Task<Void> {

  private static final int BATCH_SIZE = 100;
  private final Path selectedDirectory;
  private final DSLContext create;
  private Subscription subscription;

  public AddDirectoryTask(final Path selectedDirectory, final DSLContext create) {
    this.selectedDirectory = selectedDirectory;
    this.create = create;
  }

  @Override
  protected Void call() throws Exception {
    updateMessage("Adding '" + selectedDirectory + "' to the library...");
    pathsOf(selectedDirectory)
        .map(this::toFilePathsRecord)
        .buffer(BATCH_SIZE)
        .doOnSubscribe(s -> subscription = s)
        .subscribe(records -> create.batchInsert(records).execute());
    updateMessage("Directory added successfully");
    Thread.sleep(500);
    return null;
  }

  @Override
  protected void cancelled() {
    if (this.subscription != null) {
      this.subscription.cancel();
    }
  }

  private Flux<Path> pathsOf(final Path directory) {
    try {
      return Flux.fromStream(Files.walk(directory));
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private FilePathsRecord toFilePathsRecord(final Path path) {
    return new FilePathsRecord()
        .setId(UUID.randomUUID())
        .setPath(path.toString());
  }

}
