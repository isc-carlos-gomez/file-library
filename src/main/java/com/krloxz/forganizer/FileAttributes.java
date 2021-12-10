package com.krloxz.forganizer;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

// @Entity
public class FileAttributes {

  @Id
  private final int pathId;
  private final String name;
  private final long size;
  private final LocalDateTime lastModifiedTime;

  public FileAttributes(final int pathId, final String name, final long size, final LocalDateTime lastModifiedTime) {
    this.pathId = pathId;
    this.name = name;
    this.size = size;
    this.lastModifiedTime = lastModifiedTime;
  }

  public String getName() {
    return name;
  }

  public long getSize() {
    return size;
  }

  public LocalDateTime getLastModifiedTime() {
    return lastModifiedTime;
  }

}
