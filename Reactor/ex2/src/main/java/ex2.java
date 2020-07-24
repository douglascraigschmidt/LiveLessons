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
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex2 {
    public static BigFraction makeBigFraction(Random random,
                                              boolean reduced) {
        // Create a large random big integer.
        BigInteger numerator =
            new BigInteger(150000, random);

        // Create a denominator that's between 1 to 10 times smaller
        // than the numerator.
        BigInteger denominator =
            numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

        // Return a big fraction.
        return BigFraction.valueOf(numerator,
                                   denominator,
                                   reduced);
    }

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        List<BigFraction> l1 = new ArrayList<BigFraction>();
        Random r = new Random();

        for (int i = 10; i > 0; i--)
            l1.add(makeBigFraction(r, true));

        for(BigFraction bf : l1)
            System.out.println(bf);

        List<BigFraction> l2 = new ArrayList(l1);

        Collections.sort(l1);

        System.out.println("Collections.sort(l)");
        for(BigFraction bf : l1)
            System.out.println(bf);

        HeapSort.sort(l2);

        System.out.println("HeapSort.sort(l)");
        for(BigFraction bf : l2)
            System.out.println(bf);
        /*
        Flux<Object> fluxAsyncBackp = Flux.create((FluxSink<Object> emitter) -> {

                // Publish 1000 numbers
                for (int i = 0; i < 1000; i++) {
                    System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                    // BackpressureStrategy.ERROR will cause MissingBackpressureException when
                    // subscriber can't keep up. So handle exception & call error handler.
                    emitter.next(i);
                }
                // When all values or emitted, call complete.
                emitter.complete();

            }, FluxSink.OverflowStrategy.ERROR);

        fluxAsyncBackp.subscribeOn(Schedulers.elastic()).publishOn(Schedulers.elastic()).subscribe(i -> {
                // Process received value.
                System.out.println(Thread.currentThread().getName() + " | Received = " + i);
            }, e -> {
                // Process error
                System.err.println(Thread.currentThread().getName() + " | Error = " + e.getClass().getSimpleName() + " "
                                   + e.getMessage());
            });

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);
        */
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getName()
                           + "] "
                           + string);
    }
}
