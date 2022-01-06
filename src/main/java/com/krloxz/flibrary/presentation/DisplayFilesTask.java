package com.krloxz.flibrary.presentation;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_PATHS;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javafx.concurrent.Task;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class DisplayFilesTask extends Task<List<FileItem>> {

  private final DSLContext create;

  public DisplayFilesTask(final DSLContext create) {
    this.create = create;
  }

  @Override
  protected List<FileItem> call() throws Exception {
    return Flux.fromStream(
        this.create.select(DSL.asterisk())
            .from(FILE_PATHS)
            .limit(1000)
            .fetchStreamInto(FILE_PATHS))
        .subscribeOn(Schedulers.boundedElastic())
        .map(path -> new FileItem(path.getPath(), 1000))
        .collectList()
        .block();
  }

}
