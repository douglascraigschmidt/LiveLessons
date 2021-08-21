import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example demonstrates various features of Project Loom,
 * including virtual threads and structured concurrency.
 */
public class ex35 {
    /**
     * The number of times to recurse.
     */
    private static final int sMAX = 500;

    /**
     * The number of virtual threads to create/start.
     */
    private static final int sNUMBER_OF_THREADS = 10_000;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        example2StructuredConcurrency();
        startManyVirtualThreads();
    }

    /**
     * Demonstrate Project Loom structured concurrency.
     */
    public static void example2StructuredConcurrency() {
        // Create a List to hold the futures. 
        List<Future<Integer>> futures = null;

        // Create a new scope for executing virtual threads.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            futures = Stream
                // Create a stream containing large numbers.
                .of(999_023_101, 998_203_141, 989_301_301, 999_999_977)

                // Concurrently check the primality of each number.
                .map(primeCandidate -> executor
                        // Submit each check to the executor.
                          .submit(() -> {
                                  int result = isPrime(primeCandidate);
                                  System.out.println("Thread = " 
                                                     + Thread.currentThread().getId()
                                                     + " "
                                                     + primeCandidate
                                                     + " = " 
                                                     + result);
                                  return result;
                              })
                     )

                // Trigger intermediate processing and collect
                // the results into a List.
                .collect(toList());
        } 

        System.out.println("Thread = " + Thread.currentThread().getId());

        // Print the results.  The future values will return immediately
        // since the previous scope won't exit until all tasks are done.
        futures.forEach(f -> System.out
                        .println("result = "
                                 + ExceptionUtils.rethrowSupplier(f::get).get()));
    }

    /**
     * Demonstrate how to create and start many virtual threads using
     * Project Loom.
     */
    private static void startManyVirtualThreads() {
        List<Thread> threads = IntStream
            // Generate a range of ints.
            .rangeClosed(1, sNUMBER_OF_THREADS)

            // Print out a diagnostic every 1,000 ints.
            .peek(i -> {
                if (i % 1_000 == 0)
                    System.out.println(i + " thread started");
            })

            // Make a new virtual thread for each int.
            .mapToObj(__ -> makeThread(() -> looper(1, sMAX)))

            // Start each virtual thread.
            .peek(Thread::start)

            // Collect the Thread objects into a List.
            .collect(toList());

        // Join all the threads
        threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));
    }

    /**
     * Burn CPU time doing a recursive "loop" until count > {@code max}.
     *
     * @param count The current count
     * @param max The max number of times to recurse
     */
    private static void looper(int count, int max) {
        // Bail out of recursion when count > max.
        if (count > max)
            return;

        // Sleep for a short amount of time.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Print a diagnostic ever 100 recursions.
        if (count % 100 == 0)
            System.out.println("Thread id: "+ Thread.currentThread().getId() +" : "+ count);

        // Call looper recursively.
        looper(count + 1, max);
    }

    /**
     * A factory method that makes a new unstarted virtual thread that
     * runs the given {@code runnable}.
     */
    public static Thread makeThread(Runnable runnable) {
        return Thread.ofVirtual().unstarted(runnable);
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    public static int isPrime(int primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (n / factor * factor == n)
                    return factor;

        return 0;
    }
}
