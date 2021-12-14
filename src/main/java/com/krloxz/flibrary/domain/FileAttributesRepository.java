package com.krloxz.flibrary.domain;

import java.util.List;

import reactor.core.publisher.Flux;

public interface FileAttributesRepository {

  Flux<FileAttributes> saveAll(List<FileAttributes> attributesList);

}
