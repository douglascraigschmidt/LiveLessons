package utils;

import java.util.function.Supplier;

/**
 * This class provides asynchronous and synchronous computation of
 * method execution times.
 */
public class RunTimer {
    /**
     * This object maintains information on all the timers.
     */
    private static final TimerManager sTimerManager =
        new TimerManager();

    /**
     * Call {@code supplier.get()} and time how long it takes to run.
     *
     * @param supplier The {@link Supplier} containing the code to run
     * @param methodName The name of the method being run
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     * @return The result returned by {@code supplier.get()}
     */
    public static <U> U startTimeRun(Supplier<U> supplier,
                                     String methodName,
                                     long invocationId) {
        // Start recording the timing for this method.
        sTimerManager.startTimingRecord(methodName, invocationId);

        // Run the code that's being timed.
        return supplier.get();
    }

    /**
     * Call {@code runnable.run()} and time how long it takes to run.
     *
     * @param runnable The {@link Runnable} containing the code to run
     * @param methodName The name of the method being run
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     */
    public static void startTimeRun(Runnable runnable,
                                    String methodName,
                                    long invocationId) {
        // Start recording the timing for this method.
        sTimerManager.startTimingRecord(methodName, invocationId);

        // Run the code that's being timed.
        runnable.run();
    }

    /**
     * Stop timing {@code methodName}.
     *
     * @param methodName The name of the method being tested
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     */
    public static void stopTimeRun(String methodName,
                                   long invocationId) {
        // Stop recording the timing for this method.
        sTimerManager.stopTimingRecord(methodName, invocationId);
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to run.
     * This method supports synchronous timings.
     *
     * @param supplier The {@link Supplier} containing the code to run
     * @param methodName The name of the method being tested
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     * @return The result returned by {@code supplier.get()}
     */
    public static <U> U timeRun(Supplier<U> supplier,
                                String methodName,
                                long invocationId) {
        // Take a snapshot of the start time.
        long startTime = System.nanoTime();

        // Run the code that's being timed.
        U result = supplier.get();

        // Stop recording the timing for this method.
        sTimerManager.stopTimingRecord(methodName, invocationId, startTime);

        // Return the result from the code that was being timed.
        return result;
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to run.
     * This method supports synchronous timings.
     *
     * @param runnable The {@link Runnable} containing the code to run
     * @param methodName The name of the method being tested
     * @param invocationId A unique value corresponding to the method
     *                     invocation
     */
    public static void timeRun(Runnable runnable,
                               String methodName,
                               long invocationId) {
        // Take a snapshot of the start time.
        long startTime = System.nanoTime();

        // Run the code that's being timed.
        runnable.run();

        // Stop recording the timing for this method.
        sTimerManager.stopTimingRecord(methodName, invocationId, startTime);
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         the method runs ordered from fastest to slowest
     */
    public static String getTimingResults() {
        return sTimerManager.getTimingResults();
    }
}
