package utils;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * This class simplifies the asynchronous computation of execution
 * times.
 */
public class AsyncRunTimer {
    /**
     * Keep track of which method performed the best.
     */
    private static Map<String, Long> mResultsMap = new ConcurrentHashMap<>();

    /**
     * Call {@code supplier.get()} and time how long it takes to run.
     *
     * @return The result returned by {@code supplier.get()}
     */
    public static <U> U startTimeRun(Supplier<U> supplier,
                                     String testName) {
        // Store the start time into the results map.
        mResultsMap.put(testName, System.nanoTime());
        return supplier.get();
    }

    /**
     * Call {@code runnable.run()} and time how long it takes to run.
     */
    public static void startTimeRun(Runnable runnable,
                                    String testName) {
        // Store the start time into the results map.
        mResultsMap.put(testName, System.nanoTime());
        runnable.run();
    }

    /**
     * Stop timing the test run.
     */
    public static void stopTimeRun(String testName) {
        mResultsMap.put(testName,
                (System.nanoTime() - mResultsMap.get(testName)) / 1_000_000);
    }


    /**
     * @return A string containing the timing results for all the test runs
     * ordered from fastest to slowest.
     */
    public static String getTimingResults() {
        StringBuilder stringBuffer =
            new StringBuilder();

        stringBuffer.append("\nPrinting ")
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
                 -> new AbstractMap.SimpleImmutableEntry<>
                 (entry.getValue(),
                  entry.getKey()))

            // Sort the stream by the timing results (key).
            .sorted(Comparator.comparing(AbstractMap.SimpleImmutableEntry::getKey))

            // Append the entries in the sorted stream.
            .forEach(entry -> stringBuffer
                     .append("")
                     .append(entry.getValue())
                     .append(" executed in ")
                     .append(entry.getKey())
                     .append(" msecs\n"));

        return stringBuffer.toString();
    }
}
