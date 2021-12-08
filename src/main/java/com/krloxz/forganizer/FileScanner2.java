package com.krloxz.forganizer;

import static com.krloxz.forganaizer.infra.jooq.library .Tables.FILES;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import org.jooq.DSLContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;

@Component
public class FileScanner2 {

  private final DSLContext create;

  public FileScanner2(final DSLContext create) {
    this.create = create;
  }

  @Async
  public CompletableFuture<Void> scan(final Path file) {
    try {
      final BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
      if (attributes.isDirectory()) {
        return CompletableFuture.completedFuture(null);
      }
      final String path = file.toAbsolutePath().normalize().toString();
      final LocalDateTime lastModifiedTime =
          LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault());

      final boolean alreadyScanned =
          this.create.fetchExists(FILES, FILES.PATH.eq(path)/* .and(FILES.LAST_MODIFIED_TIME.eq(lastModifiedTime)) */);
      if (!alreadyScanned) {
        final var hash = MoreFiles.asByteSource(file).hash(Hashing.sha256()).toString();
        this.create.insertInto(FILES)
            .set(FILES.PATH, path)
            // .set(FILES.NAME, file.getFileName().toString())
            // .set(FILES.SIZE, attributes.size())
            // .set(FILES.LAST_MODIFIED_TIME, lastModifiedTime)
            // .set(FILES.HASH, hash)
            .execute();
      }

    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
    return CompletableFuture.completedFuture(null);
  }

}
