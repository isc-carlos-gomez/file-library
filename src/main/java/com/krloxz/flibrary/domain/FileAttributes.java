package com.krloxz.flibrary.domain;

import java.time.LocalDateTime;

public record FileAttributes(FilePathId pathId, String name, long size, LocalDateTime lastModifiedTime) {
  // Empty
}
