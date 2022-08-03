import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This example shows how to collect results using the Project Reactor
 * {@link ParallelFlux} class.  It also demonstrates {@link Sinks.Many}.
 */
public class ex6 {
    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) throws InterruptedException {
        // The number of items to emit.
        int length = 10;

        // The approximate size of each batch.
        int batchSize = 3;

        // The count used to initialize the CountDownLatch.
        int latchCount = length / batchSize;

        // Initialize this barrier synchronizer with the given latch
        // count.
        CountDownLatch latch = new CountDownLatch(latchCount);

        display("latchCount = "
                + latchCount
                + " batchSize = "
                + batchSize);

        // Create a Sink that transmits only newly pushed data to its
        // subscriber.
        Sinks.Many<Integer> sink =
            Sinks.many().unicast().onBackpressureBuffer();

        // Atomically sum the number of items processed per rail.
        AtomicInteger sum = new AtomicInteger(0);

        // Create a Queue that can be updated concurrently.
        Queue<Integer> results =
            new ConcurrentLinkedQueue<>();

        Flux
            // Return a Flux view of this Sink.
            .from(sink.asFlux())

            // Publish on a thread in the parallel thread pool.
            .publishOn(Schedulers.parallel())

            // Display each integer that's published.
            .doOnNext(integer -> 
                      display("integer = "
                              + integer))
            // Create a ParallelFlux with batchSize rails.
            .parallel(latchCount)

            // Run each rail in the parallel thread pool.
            .runOn(Schedulers.parallel())

            // Collect the results into an ArrayList.
            .collect(ArrayList<Integer>::new, List::add)

            // This terminal operator is called for each batch.
            .subscribe(integers -> {
                    // Increment sum with the number of integers
                    // received from the completed rail.
                    sum.addAndGet(integers.size());

                    display("sum = "
                            + sum.get()
                            + " ints.size() = "
                            + integers.size()
                            + " "
                            + integers.toString());

                    // Atomically add the contents of integers to results.
                    results.addAll(integers);

                    // Decrement the latch by one.
                    latch.countDown();
                });

        display("Starting test");
        
        Flux
            // Iterate 'length' times.
            .range(1, length)
            
            // Subscribe to the Sink.
            .subscribe(sink::tryEmitNext,
                       sink::tryEmitError,
                       sink::tryEmitComplete);

        display("Awaiting test completion");

        // Block until all the processing completes.
        latch.await();

        // Sort the results via a TreeSet.
        Set<Integer> sorted = new TreeSet<>(results);

        // Print the sorted results.
        display("results = "
                + sorted.toString());

        // Determine whether the test succeeded or failed.
        if (sum.get() == length)
            display("Test success, sum = "
                    + sum.get()
                    + " length = "
                    + length);
        else
            display("Test failure, sum = "
                    + sum.get()
                    + " length = "
                    + length);
    }

    /**
     * Display the {@code output}.
     */
    private static void display(String output) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "]: "
                           + output);
    }
}
