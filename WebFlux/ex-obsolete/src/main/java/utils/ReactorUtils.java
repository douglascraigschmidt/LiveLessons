package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * A utility class containing helpful methods for manipulating various
 * Reactor features.
 */
public class ReactorUtils {
    /**
     * A utility class should always define a private constructor.
     */
    private ReactorUtils() {
    }

    /**
     * @return Schedule a mono to run on the common fork-join pool.
     */
    public static <T> Function<Mono<T>, Mono<T>> commonPoolMono() {
        return mono -> mono
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));
    }

    /**
     * @return Schedule a parallel flowable to run on the common
     * fork-join pool.
    public static <T> ParallelTransformer<T, T> commonPoolParallelFlowable() {
        return observable -> observable
            .runOn(Schedulers.from(ForkJoinPool.commonPool()));
    }
     */

    /**
     * @return Schedule an flux to run on the common fork-join pool.
     */
    public static <T> Function<Flux<T>, Flux<T>> commonPoolFlux() {
        return flux -> flux
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));
    }

    /**
     * Conditionally enable concurrent processing if {@code parallel}
     * is true, otherwise, use sequential processing.
     *
     * @return {@code commonPoolFlux()} if {@code parallel} is
     * true, else {@code callingFlux()}.
     */
    public static <T> Function<Flux<T>, Flux<T>> concurrentFluxIf(boolean parallel) {
        if (parallel)
            return ReactorUtils.commonPoolFlux();
        else
            return ReactorUtils.callingFlux();
    }

    /**
     * @return Schedule an flux to run on the calling thread.
     */
    public static <T> Function<T, T> callingFlux() {
        // No-op!!
        return flux -> flux;
    }

    /**
     * Convert {@code mono} into a "hot" mono that doesn't
     * regenerate values seen by earlier subscribers.
     *
     * @param mono The mono to make "hot"
     * @return A "hot" mono.
    public static <T> MonoSubject<T> makeHotMono(Mono<T> mono) {
        MonoSubject<T> subject = MonoSubject.create();
        mono.subscribe(subject);
        return subject;
    }
     */

    /**
     * Use {@code Flux.just()} to emit {@code item} either
     * concurrently or sequentially based on {@code parallel} flag.
     *
     * @param item Item to emit via {@code Flux.just()}
     * @param parallel True if emit concurrently, false if emit
     * @return An flux that will be emitted concurrenty or sequentially.
     */
    public static <T> Flux<T> justConcurrentIf(T item, boolean parallel) {
        return Flux
                // Just omit this one item.
                .just(item)

                // Conditionally convert to run concurrently.
                .transformDeferred(ReactorUtils.concurrentFluxIf(parallel));
    }

    /**
     * Use {@code Flux.fromIterable()} to emit {@code item} either
     * concurrently or sequentially based on {@code parallel} flag.
     *
     * @param item Item to emit via {@code Flux.fromIterable()}
     * @param parallel True if emit concurrently, false if emit
     * @return An flux that will be emitted concurrenty or sequentially.
     */
    public static <T extends Iterable<? extends T>> Flux<T> fromIterableConcurrentIf(T item, boolean parallel) {
        return Flux
                // Just omit this one item.
                .fromIterable(item)

                // Conditionally convert to run concurrently.
                .transformDeferred(ReactorUtils.concurrentFluxIf(parallel));
    }

    /**
     * Use {@code Mono.fromCallable()} to emit {@code item} concurrently.
     *
     * @param item Item to emit via {@code Mono.fromCallable()}
     * @return A mono that will be emitted concurrenty
     */
    public static <T> Mono<T> fromCallableConcurrent(Callable<T> item) {
          return Mono
                  // Just omit this one item.
                  .fromCallable(item)

                  // Conditionally convert to run concurrently.
                  .transformDeferred(ReactorUtils.commonPoolMono());
      }

    /**
     * Emit {@code collection} as a parallel flux that runs in the
     * common fork-join pool.
     *
     * @param iterable The iterable whose contents will be processed in parallel
     * @return A parallel flux running on the common fork-join pool
     */
    public static <T> ParallelFlux<T> fromIterableParallel(Iterable<T> iterable) {
        return Flux
            // Convert collection into a flux.
            .fromIterable(iterable)

            // Create a parallel flux.
            .parallel()

            // Run this flow of operations in the common fork-join pool.
            .runOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));
    }

    /**
     * Generate {@code count} instances of what's returned by
     * {@code supplier.get()}
     * @param supplier Generates a value
     * @param count Number of values to generate
     * @return A flux that contains the results of the generator
     */
    public static <T> Flux<T> generate(Supplier<T> supplier,
                                       long count) {
        return Flux
            // Create a generator.
            .create(sink -> {
            LongStream
                // Run from 1 to count.
                .rangeClosed(1, count)
                // Generate the next item and emit it.
                .forEach(i -> sink.next(supplier.get()));

            // Indicate we're done.
            sink.complete();
        });
    }

    /**
     * Generate an infinite stream of instances of what's returned by
     * {@code supplier.get()}
     * @param supplier Generates a value
     * @return A flux that contains the results of the generator
     */
    public static <T> Flux<T> generate(Supplier<T> supplier) {
        return Flux
                // Create an infinite generator.
                .create(sink -> sink.onRequest(size -> {
                    sink.next(supplier.get());
                }));
    }

    /**
     * Conditionally enable logging if {@code log} is true, otherwise,
     * don't log.
     */
    public static <T> Function<Flux<T>, Flux<T>> logIf(boolean log) {
        if (log)
            return Flux::log;
        else
            return flux -> flux;
    }

    /**
     * The random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * Simulate a random delay between 0.0 and 0.5 seconds.
     */
    public static void randomDelay() {
        int delay = sRandom.nextInt(500);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
