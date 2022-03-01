import tests.StressTestMemoizers;
import tests.TestAsyncMemoizer;
import tests.TestCallableMemoizer;
import tests.TestSupplyAsyncMemoizer;
import utils.Options;

import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

/**
 * This example shows various ways to implement and apply synchronous
 * and asynchronous memoizers using the Java ExecutorService,
 * functional interfaces, streams, and completable futures.
 * Memoization is described at
 * https://en.wikipedia.org/wiki/Memoization.
 */
public class ex25 {
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) throws InterruptedException {
        System.out.println("Starting ex25 test");
   
        // Parse the arguments.
        Options.instance().parseArgs(argv);

        // Run all the tests.
        ex25 t = new ex25();

        System.out.println("Finishing ex25 test");
    }

    /**
     * Run all the tests.
     */
    ex25() {
        // Generate a list of random numbers used by all the tests.
        var randomNumbers = makeRandomNumbers();

        List<Runnable> tests = List
            .of (// Stress-test multiple synchronous Memoizer
                 // implementations to see how they perform when run
                 // concurrently.
                 new StressTestMemoizers(randomNumbers),

                 // Synchronously check the primality of count random
                 // numbers using an ExecutorService with a fixed-size
                 // thread pool.
                 new TestCallableMemoizer(randomNumbers),

                 // Asynchronously check the primality of count random
                 // numbers using CompletableFuture.supplyAsync() with
                 // the common fork-join pool.
                 new TestSupplyAsyncMemoizer(randomNumbers),

                 // Asynchronously check the primality of count random
                 // numbers using the AsyncMemoizer.
                 new TestAsyncMemoizer(randomNumbers));

        // Run all the tests.
        tests.forEach(Runnable::run);
    }

    /**
     * A factory method that makes a {@link List} of random numbers.
     *
     * @return A {@link List} of random numbers
     */
    private List<Long> makeRandomNumbers() {
        var randomNumberCount = Options.instance().randomNumberCount();
        var maxValue = Options.instance().maxValue();
        return new Random()
            // Generate random numbers within the designated range.
            .longs(randomNumberCount,
                   maxValue - randomNumberCount,
                   maxValue)

            // Convert longs to Longs.
            .boxed()

            // Collect the random numbers into a list.
            .collect(toList());
    }
}
