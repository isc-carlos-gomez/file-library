package com.krloxz.flibrary.infra;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.krloxz.flibrary.domain.FileAttributes;
import com.krloxz.flibrary.domain.FileAttributesRepository;
import com.krloxz.forganaizer.infra.jooq.library.tables.records.FileAttributesRecord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class JooqFileAttributesRepository implements FileAttributesRepository {

  private final DSLContext create;

  public JooqFileAttributesRepository(final DSLContext create) {
    this.create = create;
  }

  public Flux<FileAttributes> saveAll(final List<FileAttributes> attributesList) {
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
