package com.krloxz.flibrary.application;

import java.util.concurrent.atomic.AtomicLong;

public class WorkTracker {

  private final long totalWork;
  private final AtomicLong workDone;

  public WorkTracker(final long totalWork) {
    this.totalWork = totalWork;
    this.workDone = new AtomicLong();
  }

  public WorkProgress reportProgress(final long progress) {
    return new WorkProgress(workDone.addAndGet(progress), totalWork);
  }

  public WorkProgress reportProgress() {
    return reportProgress(1);
  }

}
