import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 */
public class ex2 {
    private static final Random sRANDOM = new Random(0);

    private static final AtomicInteger sPENDING_ITEM_COUNTER =
        new AtomicInteger(0);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        Scheduler scheduler =
            Schedulers.newParallel("main", 1);

        CountDownLatch latch = new CountDownLatch(1);

        nonEmptyPull(scheduler)
            .flatMapIterable(items -> items)
            .doOnNext(___ -> sPENDING_ITEM_COUNTER.incrementAndGet())
            .concatMap(item -> consume(scheduler, item))
            .subscribeOn(Schedulers.single())
            .doOnTerminate(latch::countDown)
            .subscribe(item -> display("completed item: " + item),
                       error -> display("failure" + error),
                       () -> display("completed"));

        display("waiting");
        latch.await();
        scheduler.dispose();
    }

    private static Flux<List<Integer>> nonEmptyPull(Scheduler scheduler) {
        return Flux
            .<Mono<List<Integer>>>create(sink -> sink.onRequest(size -> {
                        Mono<List<Integer>> output = pull(size)
                            .filter(Predicate.not(List::isEmpty))
                            .switchIfEmpty(Mono
                                           .just(Collections.<Integer>emptyList())
                                           .delayElement(Duration.ofSeconds(1), scheduler)
                                           .doOnSubscribe(___ -> display("delaying")));
                        sink.next(output);
                    }))
            .concatMap(Function.identity(), 1)
            .filter(Predicate.not(List::isEmpty))
            .doOnNext(items -> display("got items: " + items))
            .take(20);
    }

    private static Mono<List<Integer>> pull(long size) {
        boolean empty = sRANDOM.nextBoolean();
        if (empty) {
            display("returning empty");
            return Mono.empty();
        }
        long effectiveSize = Math.min(size, 10);
        List<Integer> items = LongStream
            .range(0, effectiveSize)
            .mapToObj(___ -> sRANDOM.nextInt(100))
            .collect(Collectors.toList());
        display("size: "
                + size
                + ", effective size: "
                + effectiveSize
                + " items: "
                + items);
        return Mono.just(items);
    }

    private static Mono<Void> consume(Scheduler scheduler, int item) {
        return Mono
            .delay(Duration.ofMillis(2_000), scheduler)
            .then()
            .doOnSubscribe(___ ->
                           display("consuming item: " + item))
            .doOnTerminate(() -> {
                    int pendingItemCount = sPENDING_ITEM_COUNTER.decrementAndGet();
                    display("consumed item: "
                            + item
                            + ", pendingItemCount: "
                            + pendingItemCount);
                });
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }
}
