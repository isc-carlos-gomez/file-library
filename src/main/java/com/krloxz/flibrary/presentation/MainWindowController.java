package com.krloxz.flibrary.presentation;

import static com.krloxz.forganaizer.infra.jooq.library.Tables.FILE_PATHS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.controlsfx.dialog.ProgressDialog;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import com.krloxz.flibrary.domain.FilePath;
import com.krloxz.forganaizer.infra.jooq.library.tables.records.FilePathsRecord;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class MainWindowController {

  private final DSLContext dslContext;
  private final TreeItem<FileItem> root;

  @FXML
  private TreeTableView<FileItem> table;

  @FXML
  private TreeTableColumn<FileItem, String> pathColumn;

  @FXML
  private TreeTableColumn<FileItem, Long> sizeColumn;

  public MainWindowController(final DSLContext create) {
    this.dslContext = create;
    root = new TreeItem<>(new FileItem("/", 0));
  }

  @FXML
  public <T> void initialize() {
    final TreeItem<FileItem> root = new TreeItem<>(new FileItem("/", 0));
    this.table.setRoot(root);
    this.table.setShowRoot(false);
    this.pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("path"));
    this.sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));

    Flux.fromStream(
        this.create.select(DSL.asterisk())
            .from(FILE_PATHS)
            .limit(1000)
            .fetchStreamInto(FILE_PATHS))
        .subscribeOn(Schedulers.boundedElastic())
        .map(record -> new FileItem(record.getPath(), 1_000))
        .map(TreeItem::new)
        .buffer()
        .subscribe(items -> root.getChildren().addAll(items));

    // DisplayFilesTask displayFilesTask = new DisplayFilesTask(create);
    // displayFilesTask.setOnSucceeded(
    // event ->
    // root.getChildren().addAll(displayFilesTask.getValue().stream().map(TreeItem::new).toList()));
    // new Thread(displayFilesTask).start();

    // WITH groups AS
    // (SELECT hash, COUNT(hash) count FROM files
    // GROUP BY hash)
    // SELECT * FROM groups g
    // JOIN files f ON f.hash = g.hash
    // WHERE g.count > 1

    // } catch (final IOException e) {
    // throw new IllegalStateException(e);
    // }
  }

  @FXML
  private void addDirectory(final Event event) throws IOException {
    final Window window = this.table.getScene().getWindow();
    final DirectoryChooser chooser = new DirectoryChooser();
    final Path selectedDirectory = chooser.showDialog(window).toPath();

    final Service<Void> service = new Service<Void>() {

      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {

          private Subscription subscription;

          @Override
          protected Void call() throws Exception {
            updateMessage("Adding '" + selectedDirectory + "' to the library...");
            pathsOf(selectedDirectory)
                .map(this::toRecord)
                .buffer(100)
                .subscribe(records -> create.batchInsert(records).execute());
            updateMessage("Directory added successfully");
            Thread.sleep(500);

            updateMessage("Scanning file attributes...");
            fileScanner.scan()
                .doOnSubscribe(s -> subscription = s)
                .doOnNext(progress -> updateMessage(progress.workDone() + " / " + progress.totalWork()))
                .doOnNext(progress -> updateProgress(progress.workDone(), progress.totalWork()))
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

          private FilePathsRecord toRecord(final Path path) {
            return new FilePathsRecord()
                .setId(UUID.randomUUID())
                .setPath(path.toString());
          }

        };
      }

      @Override
      protected void failed() {
        System.err.println("FAILED");
        getException().printStackTrace();
      }

    };
    service.start();
    final ProgressDialog progressDialog = new ProgressDialog(service);
    progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).addEventFilter(ActionEvent.ACTION,
        e -> service.cancel());
    progressDialog.showAndWait();

    // this.create.select(DSL.asterisk())
    // .from(FILES)
    // .fetchStreamInto(FILES)
    // .forEach(record -> {
    // this.table.getRoot().getChildren().add(new TreeItem<>(new
    // FileItem(record.getPath(), 0)));
    // });
  }

  private Flux<Path> pathsOf(final Path directory) {
    try {
      return Flux.fromStream(Files.walk(directory));
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
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

  private FilePathsRecord toRecord(final FilePath path) {
    return new FilePathsRecord()
        .setId(path.id().value())
        .setPath(path.value());
  }

}
