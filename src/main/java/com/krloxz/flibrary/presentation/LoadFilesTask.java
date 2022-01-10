package com.krloxz.flibrary.presentation;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_PATHS;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class LoadFilesTask extends Task<List<TreeItem<FileItem>>> {

  private final DSLContext create;

  public LoadFilesTask(final DSLContext create) {
    this.create = create;
  }

  @Override
  protected List<TreeItem<FileItem>> call() throws Exception {
    updateMessage("Loading library files...");
    return Flux.fromStream(
        this.create.select(DSL.asterisk())
            .from(FILE_PATHS)
            .limit(1000)
            .fetchStreamInto(FILE_PATHS))
        .subscribeOn(Schedulers.boundedElastic())
        .map(path -> new FileItem(path.getPath(), 1000))
        .map(TreeItem::new)
        .collectList()
        .block();
  }

}
