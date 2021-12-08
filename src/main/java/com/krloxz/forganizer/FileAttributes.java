package com.krloxz.forganizer;

import java.time.LocalDateTime;

public record FileAttributes(String name, long size, LocalDateTime lastModifiedTime) {

}
