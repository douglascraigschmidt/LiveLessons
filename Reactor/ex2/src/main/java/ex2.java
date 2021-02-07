import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.HeapSort;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex2 {
     /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        Flux<Integer> o1 = Flux.just(1, 2, 3,4);
        Mono<Integer> o2 = Mono.just(1);

        o2
                .flatMapMany(v1 -> o1
                        .flatMap(v2 -> Flux
                                .just(v2)
                                .map(__ -> v1 * v2)))

        .subscribe(value -> System.out.println("value = " + value));

        Flux
            .combineLatest(o2, o1, (a, b) -> a * b)
            .subscribe(value -> System.out.println("value = " + value));
    }
}
