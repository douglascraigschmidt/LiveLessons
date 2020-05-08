import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
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
        /*
         * Notice above -
         *
         * OverflowStrategy.ERROR - Throws MissingBackpressureException is subscriber
         * can't keep up.
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);
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
