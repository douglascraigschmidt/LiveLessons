import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactortests.ReactorTests;
import rxjavatests.RxJavaTests;
import streamstests.StreamsTests;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * This example shows...
 */
public class ex7 {
    /**
     * The total random numbers to generate.
     */
    private static final int sCount = 10_000;

    /**
     * The maximum numbers to take.
     */
    private static final int sTopK = 5;

    /**
     * Initialize the input List.
     */
    private static final List<Integer> sInput = ex7
        .makeRandomNumbers(sCount);

    /**
     * @return A {@link List} of {@code count} random {@link Integer}
     *         objects
     */
    private static List<Integer> makeRandomNumbers(int count) {
        var maxValue = Integer.MAX_VALUE;

        return new Random()
            // Generate random numbers within the designated range.
            .ints(count,
                   maxValue - count,
                   maxValue)

            // Convert primitive longs to Longs.
            .boxed()

            // Collect the random numbers into a list.
            .collect(toList());
    }

    /**
     * This test demonstrates {@link Sinks.Many}.
     */
    private static void test1() throws InterruptedException {
        // The approximate size of each batch.
        int batchSize = sCount / 10;

        // The count used to initialize the CountDownLatch.
        int latchCount = sInput.size() / batchSize;

        // Initialize this barrier synchronizer with the given latch
        // count.
        CountDownLatch latch = new CountDownLatch(latchCount);

        display("latchCount = "
                + latchCount
                + " batchSize = "
                + batchSize);

        // Atomically sum the number of items processed per rail.
        AtomicInteger sum = new AtomicInteger(0);

        //  Create a Sink that transmits only newly-pushed data to its
        // subscriber.
        Sinks.Many<Integer> sink =
            Sinks.many().unicast().onBackpressureBuffer();

        // Create a Queue that can be updated concurrently.
        Queue<Integer> results =
            new ConcurrentLinkedQueue<>();

        Flux
            // Return a Flux view of sink.
            .from(sink.asFlux())

            // Publish on a thread in the parallel thread pool.
            .publishOn(Schedulers.parallel())

            /*
            // Display each integer that's published.
            .doOnNext(integer ->
            display("integer = "
            + integer))
            */

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

        display("Starting test1");
        
        Flux
            // Convert the sInput List to a Flux.
            .fromIterable(sInput)
            
            // Subscribe to the Sink.
            .subscribe(sink::tryEmitNext,
                       sink::tryEmitError,
                       sink::tryEmitComplete);

        display("Awaiting test1 completion");

        // Block until all the processing completes.
        latch.await();

        // Sort the results via a TreeSet.
        Set<Integer> sorted = new TreeSet<>(results);

        // Print the sorted results.
        display("results = "
                + sorted.toString());

        // Determine whether the test succeeded or failed.
        if (sum.get() == sInput.size())
            display("Test1 succeeded, sum = "
                    + sum.get()
                    + " length = "
                    + sInput.size());
        else
            display("Test1 failed, sum = "
                    + sum.get()
                    + " length = "
                    + sInput.size());
    }

    /**
     *
     */
    private static void test2() throws InterruptedException {
        // The degree of parallelism for the ParallelFlux.
        int parallelism = 4;

        // Initialize this barrier synchronizer the degree of
        // parallelism.
        CountDownLatch latch = new CountDownLatch(parallelism);

        display("parallelism = "
                + parallelism);

        //  Create a Sink that transmits only newly-pushed data to its
        // subscriber.
        Sinks.Many<Integer> sink =
            Sinks.many().unicast().onBackpressureBuffer();

        // Create a Queue that can be updated concurrently.
        Queue<Integer> results =
            new ConcurrentLinkedQueue<>();

        // The number of items to take.
        int maxCount = sInput.size() / 2;

        Flux
            // Return a Flux view of sink.
            .from(sink.asFlux())

            // Publish on a thread in the parallel thread pool.
            .publishOn(Schedulers.parallel())

            // Display each integer that's published.
            /*
              .doOnNext(integer ->
              display("integer = "
              + integer))
            */

            // Create a ParallelFlux with batchSize rails.
            .parallel(parallelism)

            // Run each rail in the parallel thread pool.
            .runOn(Schedulers.parallel())

            // Collect the results into an ArrayList.
            .collect(ArrayList<Integer>::new, List::add)

            // This terminal operator is called for each batch.
            .subscribe(integers -> {
                    display("integers.size() = "
                            + integers.size());

                    // Atomically add the contents of integers to
                    // results.
                    results.addAll(integers);

                    // Decrement the latch by one.
                    latch.countDown();
                });

        display("Starting test2");

        Flux
            // Convert the sInput List to a Flux.
            .fromIterable(sInput)
            
            // Subscribe to the Sink.
            .subscribe(sink::tryEmitNext,
                       sink::tryEmitError,
                       sink::tryEmitComplete);

        display("Awaiting test2 completion");

        // Block until all the processing completes.
        latch.await();

        Flux
            // Convert the results Queue to a Flux.
            .fromIterable(results)

            // Sort the results.
            .sort(Comparator.reverseOrder())

            // Suppress duplicates.
            .distinct()

            // Limit the Flux to sTake.
            .take(sTopK)

            // Print each result.
            .doOnNext(integer ->
                      display("next integer = "
                              + integer))

            // Block until all results are processed.
            .blockLast();

        display("Test2 completed");
    }

    /**
     *
     */
    private static void test3() throws InterruptedException {
        // The degree of parallelism for the ParallelFlux.
        int parallelism = 4;

        // Initialize this barrier synchronizer the degree of
        // parallelism.
        CountDownLatch latch = new CountDownLatch(parallelism);

        display("parallelism = "
                + parallelism);

        //  Create a Sink that transmits only newly-pushed data to its
        // subscriber.
        Sinks.Many<Integer> sink =
            Sinks.many().unicast().onBackpressureBuffer();

        // The number of items to take.
        int maxCount = sInput.size() / 2;

        display("Starting test3");

        Flux
            // Convert List into a Flux.
            .fromIterable(sInput)

            // Publish on a thread in the parallel thread pool.
            .publishOn(Schedulers.parallel())

            // Display each integer that's published.
            /*
              .doOnNext(integer ->
              display("integer = "
              + integer))
            */

            // Create a ParallelFlux with batchSize rails.
            .parallel(parallelism)

            // Run each rail in the parallel thread pool.
            .runOn(Schedulers.parallel())

            // Collect the results into an ArrayList.
            .collect(ArrayList<Integer>::new, List::add)

            // Convert ParallelFlux back into a Flux.
            .sequential()

            // Merge all the ArrayLists together.
            .flatMapIterable(Function.identity())

            // Suppress duplicates.
            .distinct()
            
            // Sort the results.
            .transform(GetTopK.getTopK(sTopK))

            // Print each result.
            .doOnNext(integer ->
                      display("next integer = "
                              + integer))

            // Block until all results are processed.
            .blockLast();

        display("Test3 completed");
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
