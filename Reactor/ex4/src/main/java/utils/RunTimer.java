package utils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class simplifies the computation of execution times.
 */
public class RunTimer {
    /**
     * Keep track of which SearchStreamGang performed the best.
     */
    private static Map<String, Long> mResultsMap = new HashMap<>();

    /**
     * Keeps track of how long the test has run.
     */
    private static long sStartTime;

    /**
     * Keeps track of the execution time.
     */
    private static long mExecutionTime;

    /**
     * Start timing the test run.
     */
    private static void startTiming() {
        // Note the start time.
        sStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    private static void stopTiming() {
        mExecutionTime = (System.nanoTime() - sStartTime) / 1_000_000;
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to run.
     *
     * @return The result returned by {@code supplier.get()}
     */
    public static <U> U timeRun(Supplier<U> supplier,
                                String testName) {
        startTiming();
        U result = supplier.get();
        stopTiming();

        // Store the execution times into the results map.
        mResultsMap.put(testName,
                        mExecutionTime);

        return result;
    }

    /**
     * Call {@code runnable.run()} and time how long it takes to run.
     */
    public static void timeRun(Runnable runnable,
                               String testName) {
        startTiming();
        runnable.run();
        stopTiming();

        // Store the execution times into the results map.
        mResultsMap.put(testName,
                        mExecutionTime);
    }

    /**
     * Time {@code test} with {@code testName} using the given {@code function}.
     *
     * @param test The test to run
     * @param function The function passed to the test.
     * @param testName The name of the test.
     */
    public static void timeTest(BiFunction<Function<Integer, Integer>,
                                           String,
                                           Function<Integer, Integer>> test,
                                Function<Integer, Integer> function,
                                String testName) {
        RunTimer
            // Time how long this test takes to run.
            .timeRun(() ->
                     // Run the test using the given function.
                     test.apply(function, testName),
                     testName);
    }

    /**
     * @return A string containing the timing results for all the test runs
     * ordered from fastest to slowest.
     */
    public static String getTimingResults() {
        StringBuilder stringBuffer =
            new StringBuilder();

        stringBuffer.append("Printing ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest");

        // Print out the contents of the mResultsMap in sorted
        // order.
        mResultsMap
            // Get the entrySet for the mResultsMap.
            .entrySet()

            // Convert the entrySet into a stream.
            .stream()

            // Create a SimpleImmutableEntry containing the timing
            // results (value) followed by the test name (key).
            .map(entry
                 -> new AbstractMap.SimpleImmutableEntry<>
                 (entry.getValue(),
                  entry.getKey()))

            // Sort the stream by the timing results (key).
            .sorted(Comparator.comparing(AbstractMap.SimpleImmutableEntry::getKey))

            // Append the entries in the sorted stream.
            .forEach(entry -> stringBuffer
                     .append("\n")
                     .append(entry.getValue())
                     .append(" executed in ")
                     .append(entry.getKey())
                     .append(" msecs"));

        return stringBuffer.toString();
    }
}
