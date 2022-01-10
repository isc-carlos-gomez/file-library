package com.krloxz.flibrary.domain;

import reactor.core.publisher.Flux;

public interface FileViewRepository {

  Flux<FileView> findAll();

}
