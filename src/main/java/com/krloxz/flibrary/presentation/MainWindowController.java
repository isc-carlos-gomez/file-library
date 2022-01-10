package com.krloxz.flibrary.presentation;

import java.io.IOException;
import java.nio.file.Path;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

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
    this.table.setRoot(root);
    this.table.setShowRoot(false);
    this.pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("path"));
    this.sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));

    loadFiles();
  }

  private void loadFiles() {
    final LoadFilesTask loadFilesTask = new LoadFilesTask(dslContext);
    loadFilesTask.setOnSucceeded(
        event -> root.getChildren().addAll(loadFilesTask.getValue()));
    new Thread(loadFilesTask).start();

    // TODO Fix progress report:
    // https://stackoverflow.com/questions/16368793/how-to-reset-progress-indicator-between-tasks-in-javafx2
    // final ProgressDialog addDirectoryProgress = new ProgressDialog(loadFilesTask);
    // addDirectoryProgress.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    // addDirectoryProgress.getDialogPane().lookupButton(ButtonType.CANCEL)
    // .addEventFilter(ActionEvent.ACTION, e -> loadFilesTask.cancel());
    // addDirectoryProgress.showAndWait();

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

    final AddDirectoryTask addDirectoryTask = new AddDirectoryTask(selectedDirectory, dslContext);
    addDirectoryTask.setOnSucceeded(e -> scanFiles(e));
    new Thread(addDirectoryTask).start();

    // final ProgressDialog addDirectoryProgress = new ProgressDialog(addDirectoryTask);
    // addDirectoryProgress.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    // addDirectoryProgress.getDialogPane().lookupButton(ButtonType.CANCEL)
    // .addEventFilter(ActionEvent.ACTION, e -> addDirectoryTask.cancel());
    // addDirectoryProgress.showAndWait();
  }

  @FXML
  private void scanFiles(final Event event) {
    final ScanFilesTask scanFilesTask = new ScanFilesTask(dslContext);
    scanFilesTask.setOnSucceeded(e -> loadFiles());
    new Thread(scanFilesTask).start();

    final Region veil = new Region();
    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
    veil.setPrefSize(400, 440);
    final ProgressIndicator p = new ProgressIndicator();
    p.setMaxSize(140, 140);
    p.setStyle(" -fx-progress-color: orange;");
    // change progress color

    p.progressProperty().bind(scanFilesTask.progressProperty());
    veil.visibleProperty().bind(scanFilesTask.runningProperty());
    p.visibleProperty().bind(scanFilesTask.runningProperty());

    // stackPane.getChildren().addAll(veil, p);

    // final ProgressDialog addDirectoryProgress = new ProgressDialog(scanFilesTask);
    // addDirectoryProgress.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    // addDirectoryProgress.getDialogPane().lookupButton(ButtonType.CANCEL)
    // .addEventFilter(ActionEvent.ACTION, e -> scanFilesTask.cancel());
    // addDirectoryProgress.showAndWait();
  }

}
