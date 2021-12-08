package com.krloxz.forganizer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

class ReactorTest {

  @Test
  void flatMap() {
    // final List<String> output = new ArrayList<>();
    Flux.just("baeldung", ".", "com", "baeldung", ".", "com", "baeldung", ".", "com")
        .flatMap(s -> Flux.just(s.toUpperCase().split("")).subscribeOn(Schedulers.newParallel("parallel", 5)), 5)
        .subscribe(x -> System.out.println(Thread.currentThread().getName() + ": " + x));
    // assertThat(output).containsExactlyInAnyOrder("B", "A", "E", "L", "D", "U", "N", "G", ".", "C",
    // "O", "M");
  }

  @Test
  void test() throws InterruptedException {
    // Flux.range(1, 10)
    // .parallel()
    // .runOn(Schedulers.boundedElastic())

    // .publishOn(Schedulers.boundedElastic())

    // .flatMap(x -> Mono.defer(() -> numberString(x).subscribeOn(Schedulers.boundedElastic())), 5)
    // .sequential()
    // .subscribe(value -> System.out.println(Thread.currentThread().getName() + ": " + value));
    final Scheduler scheduler = Schedulers.boundedElastic();
    final AtomicInteger progress = new AtomicInteger();
    final Disposable disposable = Flux.range(1, 10)
        .map(i -> "file" + i)
        .flatMap(file -> toFileWithAttributes(file).subscribeOn(scheduler), 3)
        .buffer(3)
        .flatMap(files -> saveAll(files).subscribeOn(scheduler))
        .map(savedFiles -> progress.addAndGet(savedFiles.size()))
        .doOnCancel(() -> System.out.println("CANCELLED"))
        .subscribe(result -> debugThread(result));

    Thread.sleep(3000);
    disposable.dispose();
    Thread.sleep(3000);
    System.out.println("Test complete!");
  }

  private Mono<String> toFileWithAttributes(final String file) {
    // debugThread("toFileWithAttributes for " + file);
    return Mono.fromSupplier(
        () -> {
          ioDelay(1000 /* + new Random().nextInt(1000) */);
          debugThread("Finding attributes for " + file);
          return file + "WithAttributes";
        });
  }

  private void ioDelay(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (final InterruptedException e) {
      throw new IllegalArgumentException("Delay was interrupted", e);
    }
  }

  private Mono<List<String>> saveAll(final List<String> files) {
    return Mono.fromSupplier(
        () -> {
          ioDelay(1000);
          debugThread("Saving files: " + files);
          return files.stream().map(file -> file + "Saved").toList();
        });
  }

  private void debugThread(final Object message) {
    System.out.printf("Thread %s - %s\n", Thread.currentThread().getName(), message);
  }

  private Mono<String> numberString(final int number) {
    try {
      Thread.sleep(1000);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    return Mono.fromSupplier(() -> String.format("Thread %s - Number: %d", Thread.currentThread().getName(), number));
  }

  @Test
  void test2() throws InterruptedException {
    Flux.range(1, 10)
        .parallel(5)
        .runOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));

          doSomething();

          System.out.println(String.format("Finish executing %s", i));
        })
        .subscribe();
    Thread.sleep(1000);
  }

  @Test
  void test3() throws InterruptedException {
    Flux.range(1, 10)
        .parallel(5, 1)
        .runOn(Schedulers.newParallel("parallel", 10))
        .flatMap(
            i -> {
              System.out.println(String.format("Start executing %s on thread %s", i, Thread.currentThread().getName()));

              doSomething();

              System.out.println(String.format("Finish executing %s", i));

              return Mono.just(i);
            })
        .subscribe();
    Thread.sleep(1000);
  }

  @Test
  void test4() throws InterruptedException {
    final Scheduler scheduler = Schedulers.boundedElastic();

    Flux.range(1, 10)
        .flatMap(
            i -> Mono.defer(() -> {
              System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));

              doSomething();

              System.out.println(String.format("Finish executing %s", i));

              return Mono.just(i);
            }).subscribeOn(scheduler),
            5)
        .log()
        .subscribe(System.out::println);
    Thread.sleep(1000);
  }

  @Test
  void test5() throws InterruptedException {
    final Scheduler scheduler = Schedulers.newParallel("parallel", 2);

    Flux.range(1, 10)
        .flatMap(
            i -> Mono.defer(() -> {
              System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));

              return Mono.delay(Duration.ofSeconds(i))
                  .flatMap(x -> {
                    System.out.println(
                        String.format("Finish executing %s on thread %s", i, Thread.currentThread().getName()));

                    return Mono.just(i);
                  });
            }).subscribeOn(scheduler),
            5)
        .log()
        .subscribe(System.out::println);
    Thread.sleep(10000);
  }

  @Test
  void test6() throws InterruptedException {
    final Scheduler scheduler = Schedulers.newParallel("parallel", 2);

    Flux.range(1, 10)
        .flatMap(
            i -> Mono.defer(() -> {
              System.out.println(String.format("Executing %s on thread %s", i, Thread.currentThread().getName()));

              doSomething();

              System.out
                  .println(String.format("Finish executing %s on thread %s", i, Thread.currentThread().getName()));
              return Mono.just(i);
            }).subscribeOn(scheduler),
            5)
        .log()
        .subscribe(this::debugThread);
    Thread.sleep(10000);
  }

  // https://www.woolha.com/tutorials/project-reactor-processing-flux-in-parallel

  private void doSomething() {
    try {
      Thread.sleep(100);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

}
