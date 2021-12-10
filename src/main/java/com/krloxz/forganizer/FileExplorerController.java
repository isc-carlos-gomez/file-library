package com.krloxz.forganizer;

import java.io.IOException;
import java.nio.file.Path;

import org.controlsfx.dialog.ProgressDialog;
import org.jooq.DSLContext;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.HostServices;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import reactor.core.Disposable;

@Component
public class FileExplorerController {

  private final HostServices hostServices;

  @FXML
  public Label label;

  @FXML
  public Button button;

  @FXML
  public TreeTableView<FileItem> table;

  @FXML
  public TreeTableColumn<FileItem, String> pathColumn;

  @FXML
  public TreeTableColumn<FileItem, Long> sizeColumn;

  private final DSLContext create;

  public FileExplorerController(final HostServices hostServices, final FileScanner2 scanner, final DSLContext create) {
    this.hostServices = hostServices;
    this.scanner = scanner;
    this.create = create;
  }

  private final FileScanner2 scanner;

  @FXML
  public void initialize() {
    this.button.setOnAction(actionEvent -> this.label.setText(this.hostServices.getDocumentBase()));

    final TreeItem<FileItem> root = new TreeItem<>(new FileItem("/", 0));
    this.table.setRoot(root);
    this.table.setShowRoot(false);

    this.pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("path"));
    this.sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));

    // try {
    // final Instant start = Instant.now();
    // final List<CompletableFuture<Void>> futures = Files
    // .walk(Paths.get("./test-files"))
    // .map(this.scanner::scan)
    // .toList();

    // futures.forEach(future -> {
    // future.whenComplete((final Void x, final Throwable t) -> {
    // System.out.println(Thread.currentThread().getName() + "complete");
    // });
    // });
    // CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    // final Instant finish = Instant.now();
    // System.out.println(Duration.between(start, finish).toSeconds());

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

  @Autowired
  private DirectoryAdder directoryAdder;

  @Autowired
  private FileScanner fileScanner;

  @FXML
  private void addDirectory(final Event event) throws IOException {
    final Window window = this.table.getScene().getWindow();
    final DirectoryChooser chooser = new DirectoryChooser();
    // chooser.setTitle("Select a directory");
    final Path selectedDirectory = chooser.showDialog(window).toPath();

    final Service<Void> service = new Service<Void>() {
      private Disposable disposable;
      private Subscription subscription;

      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            updateMessage("Starting...");
            updateMessage("Adding '" + selectedDirectory + "' to the library...");
            directoryAdder.add(selectedDirectory);
            updateMessage("Files added successfully");
            Thread.sleep(500);
            /*
            updateMessage("Scanning file attributes...");
            // disposable =
            fileScanner.scan()
                .doOnNext(progress -> updateProgress(progress, 30))
                .doOnSubscribe(s -> subscription = s)
                .blockLast();
            // .subscribe(progress -> updateProgress(progress, 1_000));
            // Thread.sleep(30_000);
            updateMessage("Scan is complete");
            */
            return null;
          }
        };
      }

      @Override
      protected void succeeded() {
        System.err.println("SUCCEEDED");
      }

      @Override
      protected void cancelled() {
        System.err.println("CANCEL clicked");
        // this.disposable.dispose();
        this.subscription.cancel();
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

    // Alert alert = new Alert(AlertType.CONFIRMATION);
    // alert.initOwner(this.table.getScene().getWindow());
    // alert.showAndWait();

    // this.create.select(DSL.asterisk())
    // .from(FILES)
    // .fetchStreamInto(FILES)
    // .forEach(record -> {
    // this.table.getRoot().getChildren().add(new TreeItem<>(new FileItem(record.getPath(), 0)));
    // });
  }

  // record Test(String path) {
  // }
  public class FileItem {

    private StringProperty path;
    private LongProperty size;

    public FileItem(final String path, final long size) {
      setPath(path);
      setSize(size);
    }

    public void setPath(final String value) {
      pathProperty().set(value);
    }

    public String getPath() {
      return pathProperty().get();
    }

    public StringProperty pathProperty() {
      if (this.path == null) {
        this.path = new SimpleStringProperty(this, "name");
      }
      return this.path;
    }

    public void setSize(final long value) {
      sizeProperty().set(value);
    }

    public long getSize() {
      return sizeProperty().get();
    }

    public LongProperty sizeProperty() {
      if (this.size == null) {
        this.size = new SimpleLongProperty(this, "lastModified");
      }
      return this.size;
    }
  }

}
