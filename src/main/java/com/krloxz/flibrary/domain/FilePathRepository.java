package com.krloxz.flibrary.domain;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FilePathRepository {

  Flux<FilePath> saveAll(List<FilePath> paths);

  Flux<FilePath> findAllWithNoAttributes();

  Mono<Integer> countAllWithNoAttributes();

}
