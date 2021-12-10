package com.krloxz.forganizer;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILES;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.krloxz.forganaizer.infra.jooq.library.tables.records.FilesRecord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class FilePathRepository {

  private final DSLContext create;

  public FilePathRepository(final DSLContext create) {
    this.create = create;
  }

  @Async
  CompletableFuture<Void> saveAll(final List<FilePath> batch) {
    final var fileRecords = batch.stream()
        .map(path -> {
          return new FilePathsRecord()
              .setPath(path..path().toString());
        })
        .toList();
    this.create.batchInsert(fileRecords).execute();
    return CompletableFuture.completedFuture(null);
  }

  Flux<File> findAllWithNoAttributes() {
    return Flux.fromStream(
        () -> this.create.select(DSL.asterisk())
            .from(FILES)
            .fetchSize(1_000)
            .stream()
            .onClose(() -> System.out.println("Closing stream"))
            .map(FILES::from)
            .map(FilesRecord::getPath)
            .map(File::new));
  }

  // Mono<Integer> countAllWithNoAttributes() {
  //   return Mono.fromCallable(() -> this.create.selectCount().from(FILES).where(FILES.pa));
  // }

}
