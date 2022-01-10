package com.krloxz.flibrary.presentation;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_ATTRIBUTES;
import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_PATHS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.reactivestreams.Subscription;

import com.krloxz.flibrary.domain.FileAttributes;
import com.krloxz.flibrary.domain.FilePath;
import com.krloxz.flibrary.domain.FilePathId;
import com.krloxz.forganaizer.infra.jooq.library.tables.records.FileAttributesRecord;

import javafx.concurrent.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ScanFilesTask extends Task<Void> {

  private static final int BATCH_SIZE = 100;
  private final DSLContext create;
  private final Scheduler scheduler;
  private Subscription subscription;

  public ScanFilesTask(final DSLContext create) {
    this.create = create;
    this.scheduler = Schedulers.boundedElastic();
  }

  @Override
  protected Void call() throws Exception {
    updateMessage("Scanning file attributes...");

    final int totalPaths = countAllWithNoAttributes().block();
    findAllWithNoAttributes()
        .flatMap(paths -> read(paths).subscribeOn(scheduler), BATCH_SIZE)
        .buffer(BATCH_SIZE)
        .flatMap(attributesList -> saveAllAttributes(attributesList).subscribeOn(scheduler))
        .doOnSubscribe(s -> subscription = s)
        .doOnNext(progress -> updateMessage(BATCH_SIZE + " / " + totalPaths))
        .doOnNext(progress -> updateProgress(BATCH_SIZE, totalPaths))
        .blockLast();
    updateMessage("Scan is complete");
    Thread.sleep(500);

    return null;
  }

  @Override
  protected void cancelled() {
    if (this.subscription != null) {
      this.subscription.cancel();
    }
  }

  public Mono<Integer> countAllWithNoAttributes() {
    return Mono.fromCallable(
        () -> this.create.selectCount()
            .from(FILE_PATHS)
            .leftAntiJoin(FILE_ATTRIBUTES).onKey()
            .fetchOne(0, Integer.class));
  }

  public Flux<FilePath> findAllWithNoAttributes() {
    return Flux.fromStream(
        () -> this.create.select(DSL.asterisk())
            .from(FILE_PATHS)
            .leftAntiJoin(FILE_ATTRIBUTES).onKey()
            .fetchSize(1_000)
            .stream()
            .map(FILE_PATHS::from)
            .map(record -> new FilePath(FilePathId.of(record.getId()), record.getPath())));
  }

  public Mono<FileAttributes> read(final FilePath filePath) {
    return Mono.fromSupplier(
        () -> {
          try {
            final Path path = Paths.get(filePath.value());
            final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            final LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(),
                ZoneId.systemDefault());
            return new FileAttributes(filePath.id(), path.getFileName().toString(), attributes.size(),
                lastModifiedTime);
          } catch (final IOException e) {
            throw new IllegalStateException("Unable to read file attributes of " + filePath, e);
          }
        });
  }

  public Flux<FileAttributes> saveAllAttributes(final List<FileAttributes> attributesList) {
    return Mono.fromCallable(
        () -> {
          final var records = attributesList.stream()
              .map(this::toRecord)
              .toList();
          this.create.batchInsert(records).execute();
          return attributesList;
        })
        .flatMapMany(Flux::fromIterable);
  }

  private FileAttributesRecord toRecord(final FileAttributes attributes) {
    return new FileAttributesRecord()
        .setPathId(attributes.pathId().value())
        .setName(attributes.name())
        .setSize(attributes.size())
        .setLastModifiedTime(attributes.lastModifiedTime());
  }

}
