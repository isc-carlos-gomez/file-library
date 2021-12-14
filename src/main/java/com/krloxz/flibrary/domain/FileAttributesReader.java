package com.krloxz.flibrary.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class FileAttributesReader {

  public Mono<FileAttributes> read(final FilePath filePath) {
    return Mono.fromSupplier(
        () -> {
          try {
            final Path path = Paths.get(filePath.value());
            final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            final LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(),
                ZoneId.systemDefault());
            return new FileAttributes(filePath.id(), path.getFileName().toString(), attributes.size(),
                lastModifiedTime);
          } catch (final IOException e) {
            throw new IllegalStateException("Unable to read file attributes of " + filePath, e);
          }
        });
  }

}
