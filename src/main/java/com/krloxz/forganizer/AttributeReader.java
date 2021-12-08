package com.krloxz.forganizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class AttributeReader {

  public Mono<File> read(final File file) {
    return Mono.fromSupplier(
        () -> {
          try {
            final BasicFileAttributes attributes = Files.readAttributes(file.path(), BasicFileAttributes.class);
            final LocalDateTime lastModifiedTime =
                LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            final var fileAttributes =
                new File.Attributes(file.path().getFileName().toString(), attributes.size(), lastModifiedTime);
            return new File(file.path(), fileAttributes);

          } catch (final IOException e) {
            throw new IllegalStateException("Unable to read file attributes of " + file, e);
          }
        });
  }

}
