package com.krloxz.flibrary.presentation;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
