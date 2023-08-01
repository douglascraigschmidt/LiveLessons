package utils;

import java.util.*;
import java.util.function.Supplier;

import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * This class simplifies the computation of execution times.
 */
public class RunTimer {
    /**
     * Keep track of which test runs performed the best.
     */
    private static final Map<String, Long> mResultsMap = new HashMap<>();

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
     * Call @a supplier.get() and time how long it takes to run.
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
     * Call @a runnable.run() and time how long it takes to run.
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
        StringBuilder stringBuffer = new StringBuilder();

        stringBuffer
            .append("Printing ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest");

        // Create a List from the mResultsMap's entrySet.
        List<Map.Entry<Long, String>> entries = new ArrayList<>();

        // Add the entries from mResultsMap to the List.
        for (var entry : mResultsMap.entrySet())
            entries.add(new SimpleImmutableEntry<>(entry.getValue(),
                                                   entry.getKey()));

        // Sort the list by the timing results (key).
        entries.sort(Map.Entry.comparingByKey());

        // Append the entries in the sorted list.
        for (var entry : entries) {
            stringBuffer.append("\n")
                .append(entry.getValue())
                .append(" executed in ")
                .append(entry.getKey())
                .append(" msecs");
        }

        // Convert the StringBuffer to a String and return it.
        return stringBuffer.toString();
    }
}
