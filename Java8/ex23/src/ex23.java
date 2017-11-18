
import utils.RunTimer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.StreamOfFuturesCollector.toFuture;

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
    private static AtomicInteger mCounter = new AtomicInteger(0);

    /**
     * This supplier increments the atomic counter, sleeps for 1
     * second, and then returns the incremented value.
     */
    private static Supplier<Integer> mSupplier = () -> {
        int result = mCounter.incrementAndGet();
        display("enter Supplier with value "
                + result);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        display("leave Supplier");
        return result;
    };

    /**
     * This function returns even numbers passed to it and null if an
     * odd number is passed to it.
     */
    private static Function<Integer, Integer> mAction = i -> {
        display("enter Function with value "
                + i);
        display("leave Function");
        return (i % 2) == 0 ? i : null;
    };

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        // A list of suppliers that return integers.
        List<Supplier<Integer>> suppliers =
            Arrays.asList(mSupplier,
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

        // A future to a stream of integer results.
        CompletableFuture<Stream<Integer>> resultsFuture = suppliers
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
            .collect(toFuture());

        resultsFuture
            // When all the futures associated with resultsFuture
            // complete then display the results.
            .thenAccept(stream ->
                        display("results = "
                                + stream
                                // Trigger intermediate processing and
                                // return a list of results.
                                .collect(toList())))

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

            // Asynchronously apply the action to the results of the
            // previous completion stage.
            .map(intFuture -> intFuture.thenApply(mAction))

            // Trigger intermediate operations and return a future to
            // a stream of results.
            .collect(toFuture());

        resultFuture
            // When all the futures associated with resultsFuture
            // complete then display the results.
            .thenAccept(stream -> 
                        display("results = " 
                                + stream
                                // Filter out any null results.
                                .filter(Objects::nonNull)

                                // Trigger intermediate processing and
                                // return a list of results.
                                .collect(toList())))

            // Block caller until all processing is complete.
            .join();

        System.out.println("<<< leave testOneJoin()\n");
    }

    /**
     * Display the {@code string} after prepending the thread id and
     * curren time.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + ", "
                           + System.currentTimeMillis()
                           + "] "
                           + string);
    }
}
