package tests;

import utils.Memoizer;
import utils.Options;
import utils.PrimeCheckers;
import utils.StatCollector;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowRunnable;

/** 
 * Stress test the ConcurrentHashMap and synchronized HashMap classes
 * to see how they perform when run concurrently.
 */
public class StressTestMemoizers
      implements Runnable {
    /**
     * The {@link List} of randomly generated numbers.
     */
    private final List<Long> mRandomNumbers;

    /**
     * The unique set of random numbers, i.e., non-duplicate.
     */
    private static HashSet<Long> mUniqueRandomNumbers;

    /**
     * Constructor initializes the fields.
     */
    public StressTestMemoizers(List<Long> randomNumbers) {
        mRandomNumbers = randomNumbers;

        // The set of unique random numbers.
        mUniqueRandomNumbers = new HashSet<>(randomNumbers);

        int totalDuplicates =
            randomNumbers.size() - mUniqueRandomNumbers.size();

        System.out.println("Total random numbers = "
                           + Options.instance().randomNumberCount()
                           + "\nTotal duplicate random numbers = "
                           + totalDuplicates
                           + "\nTotal distinct random numbers = "
                           + (Options.instance().randomNumberCount()
                              - totalDuplicates));
    }

    /**
     * Stress-test multiple synchronous Memoizer implementations to
     * see how they perform when run concurrently.
     */
    @Override
    public void run() {
        System.out.println("Running StressTestMemoizers");

        // Run with a ConcurrentHashMap. 
        stressTestMemoizer(mRandomNumbers,
                           new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                          new ConcurrentHashMap<>()),
                           "ConcurrentHashMap");

        // Run with a synchronized HashMap.
        stressTestMemoizer(mRandomNumbers,
                           new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                          Collections.synchronizedMap(new HashMap<>())),
                           "Synchronized HashMap");
    }

    /**
     * Stress-test a synchronous Memoizer implementation to see how it
     * performs when run concurrently.
     */
    private void stressTestMemoizer
        (List<Long> randomNumbers,
         Memoizer<Long, SimpleImmutableEntry<Long, Long>> primeChecker,
         String hashMapName) {
        System.out.println("Running stressTestMemoizer() for "
                           + hashMapName);

        // Ensure all the threads start at the same moment.
        CyclicBarrier cyclicBarrier =
            new CyclicBarrier(Options.instance().numberOfThreads());

        // Keep track of the statistics for this hash map.
        StatCollector statCollector = new StatCollector();

        // The range of random numbers handled by each thread.
        final int range = Options.instance().randomNumberCount()
            / Options.instance().numberOfThreads();

        // The list of threads that will perform the stress test.
        List<Thread> threads = IntStream
            // Iterate through the number of threads to create.
            .range(0, Options.instance().numberOfThreads())

            // Create a new thread to perform primality checking for a
            // range of random numbers and add it to the list.
            .mapToObj(i -> 
                      new Thread(makeRunnable
                                 // Determine lower and upper bound of
                                 // the random numbers range.
                                 (randomNumbers,
                                  i * range,
                                  (i + 1) * range,
                                  primeChecker,
                                  cyclicBarrier,
                                  statCollector)))

            // Set the uncaught exception handler.
            .peek(thread ->
                  thread.setUncaughtExceptionHandler((thr, ex) -> {
                          System.out.println("Encountered exception "
                                             + ex
                                             + " for thread "
                                             + thr);
                      }))

            // Trigger intermediate operations and collect to list.
            .collect(toList());

        // Start all the threads running.
        threads.forEach(Thread::start);

        // Wait for all the threads to finish.
        threads.forEach(rethrowConsumer(Thread::join));

        // Print the statistics for this type of hash map.
        statCollector.print(hashMapName,
                            mUniqueRandomNumbers.size());

        // Remove all the unique random numbers.
        mUniqueRandomNumbers.forEach(l -> {
                if (primeChecker.remove(l) == null)
                    System.out.println("remove failed for "
                                       + l);
            });
    }

    /**
     * This factory method creates a runnable that checks for the
     * primality of random numbers from {@code lowerBound} to {@code
     * upperBound}.
     */
    private Runnable makeRunnable
        (List<Long> randomNumbers,
         int lowerBound,
         int upperBound,
         Memoizer<Long, SimpleImmutableEntry<Long, Long>> primeChecker,
         CyclicBarrier cyclicBarrier,
         StatCollector statCollector) {
        // Return a runnable lambda.
        return rethrowRunnable(() -> {
                // Run the garbage collector before timing each test.
                System.gc();

                // Wait for all threads to start running.
                cyclicBarrier.await();

                // Record the start time.
                long startTime = System.nanoTime();

                // Iterate through the random numbers in the range and
                // test them for primality.

                IntStream
                    // Iterate through the number of threads to create.
                    .range(lowerBound, upperBound)

                    // Check whether the random number is prime or not.
                    .mapToObj(index ->
                         primeChecker.apply(Math.abs(randomNumbers.get(index))))

                    // Print the results.
                    .forEach(Options::printResult);

                // Record the stop time.
                long stopTime = (System.nanoTime() - startTime) / 1_000_000;

                // Update the statistics.
                statCollector.add(stopTime);
            });
    }
}
