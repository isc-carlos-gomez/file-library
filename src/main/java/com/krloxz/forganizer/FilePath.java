package com.krloxz.forganizer;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity(name = "file_paths")
public class FilePath {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
      name = "UUID",
      strategy = "org.hibernate.id.UUIDGenerator"
  )
  private UUID id;

  @Column(name = "path")
  private final String value;

  public FilePath(final String value) {
    this.value = value;
  }

  public UUID getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

}
