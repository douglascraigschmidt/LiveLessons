package utils;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class records how long methods take to execute and prints
 * the results ordered from fastest to slowest execution times.
 */
public class RunTimer {
    /**
     * This {@link Map} tracks how long each method took to execute.
     */
    private static final Map<String, Long> mResultsMap =
        new HashMap<>();

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
     * Record how long it takes to run {@code supplier.get()}.
     *
     * @return The result returned by {@code supplier.get()}
     */
    public static <U> U timeRun(Supplier<U> supplier,
                                String testName) {
        startTiming();

        // Invoke the supplier.
        U result = supplier.get();

        stopTiming();

        // Store the execution times into the results map.
        mResultsMap.put(testName,
                        mExecutionTime);

        // Return the result from the supplier.
        return result;
    }

    /**
     * Record how long it takes to run {@code runnable.run()}.
     */
    public static void timeRun(Runnable runnable,
                               String testName) {
        startTiming();

        // Invoke the runnable.
        runnable.run();

        stopTiming();

        // Store the execution times into the results map.
        mResultsMap.put(testName,
                        mExecutionTime);
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         the test runs ordered from fastest to slowest
     */
    public static String getTimingResults() {
        // Create a StringBuilder to hold the results.
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nPrinting ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest\n");

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
                 -> new SimpleImmutableEntry<>
                 (entry.getValue(),
                  entry.getKey()))

            // Sort the stream by the timing results (key).
            .sorted(Map.Entry.comparingByKey())

            // Append the entries in the sorted stream.
            .forEach(entry -> stringBuilder
                     .append(entry.getValue())
                     .append(" executed in ")
                     .append(entry.getKey())
                     .append(" msecs\n"));

        // Convert the StringBuilder to a String.
        return stringBuilder.toString();
    }
}
