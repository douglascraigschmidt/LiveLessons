package utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class provides asynchronous and synchronous computation of
 * method execution times.
 */
public class TimerManager {
    /**
     * This data structure that records the execution time of method
     * names.  It contains a {@link Map} that associate each method
     * name with its start time, as well as a given method name's
     * average computation time and its total number of invocations.
     */
    public record TimingRecord
        (/*
          * A {@link Map} that associates a method name's invocation
          * id with a start time.
          */
         Map<Long, Long> methodMap,

         /*
          * The average time of all the computations for a given
          * method name.
          */
         AtomicLong averageTime,

         /*
          * The total number of invocations for a given method name.
          */
         AtomicLong invocationCount) implements Comparable<TimingRecord> {

        /**
         * Compares this {@link TimingRecord} with the specified
         * {@link TimingRecord} for order using the {@code
         * averageTime()} method. Returns a negative integer, zero, or
         * a positive integer as this {@link TimingRecord} is less
         * than, equal to, or greater than the specified {@link
         * TimingRecord}.
         *
         * @param that The {@link TimingRecord} to be compared
         * @return A negative integer, zero, or a positive integer as
         *         this {@link TimingRecord}'s average time is less
         *         than, equal to, or greater than the specified
         *         {@link TimingRecord}
         */
        @Override
            public int compareTo(TimingRecord that) {
            return (int) (this.averageTime().get()
                          - that.averageTime().get());
        }
    }

    /**
     * Keep track of all the timing results.  This {@link Map}
     * associates a method name with its {@link TimingRecord}.
     */
    private final Map<String, TimingRecord> mResultsMap =
        new ConcurrentHashMap<>();

    /**
     * Start timing the execution time of the given {@code
     * methodName}.
     * 
     * @param methodName The name of the method being run
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     */
    public void startTimingRecord(String methodName,
                                  long invocationId) {
        // Lookup the TimingRecord associated with methodName.
        TimingRecord timingRecord = mResultsMap.get(methodName);

        // This handles the case where methodName wasn't in the Map.
        if (timingRecord == null) 
            timingRecord = new TimingRecord(new ConcurrentHashMap<>(),
                                            new AtomicLong(0),
                                            new AtomicLong(0));

        // Increment the invocation count.
        timingRecord.invocationCount().incrementAndGet();

        // Store the current time in the methodMap.
        timingRecord.methodMap().put(invocationId, System.nanoTime());

        // Put the methodName and timingRecord into the results Map.
        mResultsMap.put(methodName, timingRecord);
    }

    /**
     * Stop timing the execution time of the given {@code methodName}.
     * 
     * @param methodName The name of the method being run
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     * @param startTime The start time when the code was run
     */
    public void stopTimingRecord(String methodName,
                                 long invocationId,
                                 long startTime) {
        // Compute the elapsed time in microseconds.
        long elapsedTime = (System.nanoTime() - startTime ) / 1_000_000;

        // Lookup the TimingRecord associated with methodName.
        TimingRecord timingRecord = mResultsMap.get(methodName);

        // This handles the case where methodName wasn't in the Map.
        if (timingRecord == null) 
            timingRecord = new TimingRecord(new ConcurrentHashMap<>(),
                                            new AtomicLong(0),
                                            new AtomicLong(0));

        // Increment the invocation count.
        timingRecord.invocationCount().incrementAndGet();

        // Update the average time.
        timingRecord
            .averageTime()
            .set((timingRecord.invocationCount().get()
                  * timingRecord.averageTime().get()
                  + elapsedTime)
                 / (timingRecord.invocationCount().get()));

        // Put the methodName and timingRecord into the results Map.
        mResultsMap.put(methodName, timingRecord);
    }

    /**
     * Stop timing the execution time of the given {@code methodName}.
     *
     * @param methodName The name of the method being run
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     */
    public void stopTimingRecord(String methodName,
                                 long invocationId) {
        // Compute the elapsed time in microseconds.
        long currentTime = System.nanoTime();

        // Lookup the TimingRecord associated with methodName.
        TimingRecord timingRecord = mResultsMap
            .get(methodName);

        assert timingRecord != null;

        // Remove this invocation from the Map, which returns the
        // associated start time.
        long startTime = timingRecord
            .methodMap().remove(invocationId);

        // Update the average time.
        timingRecord
            .averageTime()
            .set((timingRecord.invocationCount().get()
                  * timingRecord.averageTime().get()
                  + (currentTime - startTime) / 1_000_000) 
                 / (timingRecord.invocationCount().get()));

        mResultsMap.put(methodName, timingRecord);
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         the method runs ordered from fastest to slowest
     */
    public String getTimingResults() {
        StringBuilder stringBuffer =
            new StringBuilder();

        stringBuffer.append("\nPrinting ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest\n");

        // Print the contents of the mResultsMap in sorted order.
        mResultsMap
            // Get the entrySet for the mResultsMap.
            .entrySet()

            // Convert the entrySet into a stream.
            .stream()

            // Create a SimpleEntry containing the timing
            // results (value) followed by the test name (key).
            .map(entry
                 -> new SimpleEntry<>
                 (entry.getValue(),
                  entry.getKey()))

            // Sort the stream by the timing results (key).
            .sorted(Comparator.comparing(SimpleEntry::getKey))

            // Append the entries in the sorted stream.
            .forEach(entry -> stringBuffer
                     .append(entry.getKey().invocationCount())
                     .append(" call(s) to ")
                     .append(entry.getValue())
                     .append(" executed in an average of ")
                     .append(entry.getKey().averageTime())
                     .append(" msecs\n"));

        // Convert stringBuffer to a String.
        return stringBuffer.toString();
    }
}
