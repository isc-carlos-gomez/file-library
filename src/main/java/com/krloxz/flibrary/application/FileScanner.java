package com.krloxz.flibrary.application;

import org.springframework.stereotype.Component;

import com.krloxz.flibrary.domain.FileAttributesReader;
import com.krloxz.flibrary.domain.FileAttributesRepository;
import com.krloxz.flibrary.domain.FilePathRepository;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class FileScanner {

  private static final int BATCH_SIZE = 100;
  private final FilePathRepository pathRepository;
  private final FileAttributesReader attributeReader;
  private final FileAttributesRepository attributesRepository;
  private final Scheduler scheduler;

  public FileScanner(final FilePathRepository pathRepository, final FileAttributesReader attributeReader,
      final FileAttributesRepository attributesRepository) {
    this.pathRepository = pathRepository;
    this.attributeReader = attributeReader;
    this.attributesRepository = attributesRepository;
    this.scheduler = Schedulers.boundedElastic();
  }

  public Flux<WorkProgress> scan() {
    final int totalPaths = this.pathRepository.countAllWithNoAttributes().block();
    final WorkTracker workTracker = new WorkTracker(totalPaths);
    return this.pathRepository.findAllWithNoAttributes()
        .flatMap(paths -> this.attributeReader.read(paths).subscribeOn(scheduler), BATCH_SIZE)
        .buffer(BATCH_SIZE)
        .flatMap(attributesList -> this.attributesRepository.saveAll(attributesList).subscribeOn(scheduler))
        .map(ignored -> workTracker.reportProgress());
  }

}
