package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class simplifies computing and printing execution times of tests.
 */
public class RunTimer {
    /**
     * Keep track of which test performed the best.
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
     * @return The result returned by @a supplier.get()
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
     * @return A string containing the timing results for all the test runs
     * ordered from fastest to slowest.
     */
    public static String getTimingResults() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nPrinting ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest\n");

        // Print out the contents of the mResultsMap sorted by the
        // execution time.
        mResultsMap
            // Get the entrySet for the mResultsMap.
            .entrySet()

            // Convert the entrySet into a stream.
            .stream()

            // Sort the stream by the timing results (key).
            .sorted(Map.Entry.comparingByValue())

            // Append the sorted entries into the string buffer.
            .forEach(entry -> stringBuilder
                     .append(entry.getKey())
                     .append(" executed in ")
                     .append(entry.getValue())
                     .append(" msecs\n"));

        // Convert the string buffer into a string and return it.
        return stringBuilder.toString();
    }
}
