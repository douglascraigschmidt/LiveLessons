import utils.ListOfFuturesCollector;
import utils.RunTimer;
import utils.StreamOfFuturesCollector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * This example shows the difference between calling join() on
 * intermediate completable futures (which block and thus degrade
 * performance) vs. simply making one call to join() (and thus
 * enhancing greater parallelism).  These tests demonstrate why join()
 * shouldn't be used in a stream pipeline on a CompletableFuture that
 * hasn't completed since it may impede parallelism.
 */
public class ex23 {
    /**
     * A counter used to monotonically/atomically generate a new value
     * each time it's called.
     */
    private static AtomicInteger mCounter = 
        // Start at 10.
        new AtomicInteger(10);

    /**
     * This supplier increments the atomic counter, sleeps for 1
     * second, and then returns the incremented value.
     */
    private static Supplier<Integer> mSupplier = () -> {
        int result = mCounter.incrementAndGet();
        display("enter Supplier with value "
                + result);
        sleep(1000);
        display("leave Supplier");
        return result;
    };

    /**
     * This function returns odd numbers passed to it and null if an
     * even number is passed to it.
     */
    private static Function<Integer, Integer> mAction = i -> {
        display("enter Function with value "
                + i);
        display("leave Function");
        return (i % 2) == 1 ? i : null;
    };

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        // A list of suppliers that return integers.
        // List<Supplier<Integer>>
        var suppliers =
            List.of(mSupplier,
                    mSupplier,
                    mSupplier,
                    mSupplier,
                    mSupplier,
                    mSupplier);

        // Run a test that makes multiple blocking join calls on
        // completable futures.
        RunTimer.timeRun(() -> testManyJoins(suppliers),
                         "testManyJoins()");

        // Run a test that makes just one blocking join calls on
        // completable futures.
        RunTimer.timeRun(() -> testOneJoin(suppliers),
                         "testOneJoin()");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * This test makes multiple blocking join calls when processing a
     * list of completable futures.
     */
    private static void testManyJoins(List<Supplier<Integer>> suppliers) {
        System.out.println(">>> testManyJoins()");

        // A future to a list of integer results.
        CompletableFuture<List<Integer>> resultsFuture = suppliers
            // Convert the list of suppliers into a stream of suppliers.
            .stream()

            // Run each supplier asynchronously.
            .map(CompletableFuture::supplyAsync)

            // Filter out any null results.  join() causes the call to
            // block, demonstrating the limitations of trying to use
            // filter() in conjunction with completable futures.
            .filter(intFuture -> intFuture.thenApply(mAction).join() != null)

            // Trigger intermediate operations and return a future to
            // a stream of results.
            .collect(ListOfFuturesCollector.toFuture());

        resultsFuture
            // When all the futures associated with resultsFuture
            // complete then display the results.
            .thenAcceptAsync(ex23::displayPrimes)


            // Block caller until all processing is complete.
           .join();

        System.out.println("<<< leave testManyJoins()");
    }

    /**
     * This test makes just one blocking join call when processing a
     * list of completable futures.
     */
    private static void testOneJoin(List<Supplier<Integer>> suppliers) {
        System.out.println(">>> enter testOneJoin()");

        // A future to a stream of integer results.
        CompletableFuture<Stream<Integer>> resultFuture = suppliers
            // Convert the list of suppliers into a stream of suppliers.
            .stream()

            // Run each supplier asynchronously.
            .map(CompletableFuture::supplyAsync)

            // Apply action to results of the previous completion stage.
            .map(intFuture -> intFuture.thenApply(mAction))

            // Trigger intermediate operations and return a future to
            // a stream of results.
            .collect(StreamOfFuturesCollector.toFuture());

        resultFuture
            // When all the futures associated with resultsFuture
            // complete then display the results.
            .thenAcceptAsync(stream ->
                             displayPrimes(stream
                                           // Filter out null results.
                                           .filter(Objects::nonNull)

                                           // Trigger intermediate
                                           // processing and return
                                           // a list of results.
                                           .collect(toList())))

            // Block caller until all processing is complete.
            .join();

        System.out.println("<<< leave testOneJoin()\n");
    }

    /**
     * Display elements in the {@code list} after computing which
     * numbers are prime.
     */
    private static void displayPrimes(List<Integer> list) {
        // Create a string containing all the prime numbers in the
        // list.
        String primes = list
            // Convert the list to a stream.
            .stream()

            // Remove non-prime numbers.
            .filter(ex23::isPrime)

            // Convert integers to strings.
            .map(String::valueOf)

            // Concatenate the strings.
            .collect(joining(", "));

        // Display the results.
        display("primes = "
                + primes);
    }

    /**
     * Display the {@code string} after prepending the current thread
     * and time.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + ", "
                           + System.currentTimeMillis()
                           + "] "
                           + string);
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns true if it is
     * prime or false if not.
     */
    static private boolean isPrime(Integer primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    System.out.println(""
                                       + Thread.currentThread()
                                       + " Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return false;

        return true;
    }

    /**
     * Simple sleep() wrapper that zaps annoying checked exceptions.
     */
    static void sleep(int milliseconds) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
