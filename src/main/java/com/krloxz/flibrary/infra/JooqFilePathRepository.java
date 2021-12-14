package com.krloxz.flibrary.infra;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_ATTRIBUTES;
import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_PATHS;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.krloxz.flibrary.domain.FilePath;
import com.krloxz.flibrary.domain.FilePathId;
import com.krloxz.flibrary.domain.FilePathRepository;
import com.krloxz.forganaizer.infra.jooq.library.tables.records.FilePathsRecord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class JooqFilePathRepository implements FilePathRepository {

  private final DSLContext create;

  public JooqFilePathRepository(final DSLContext create) {
    this.create = create;
  }

  public Flux<FilePath> saveAll(final List<FilePath> paths) {
    return Mono.fromCallable(
        () -> {
          final var records = paths.stream()
              .map(this::toRecord)
              .toList();
          this.create.batchInsert(records).execute();
          return paths;
        })
        .flatMapMany(Flux::fromIterable);
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

  public Mono<Integer> countAllWithNoAttributes() {
    return Mono.fromCallable(
        () -> this.create.selectCount()
            .from(FILE_PATHS)
            .leftAntiJoin(FILE_ATTRIBUTES).onKey()
            .fetchOne(0, Integer.class));
  }

  private FilePathsRecord toRecord(final FilePath path) {
    return new FilePathsRecord()
        .setId(path.id().value())
        .setPath(path.value());
  }

}
