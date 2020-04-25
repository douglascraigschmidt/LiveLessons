package utils;

import folder.Dirent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

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
}
