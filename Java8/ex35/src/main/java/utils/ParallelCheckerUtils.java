package utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Java utility class contains a static method that determines
 * how many threads are used to process an inner stream.
 */
public final class ParallelCheckerUtils {
    /**
     * These constants are used to differentiate different results
     * from the {@code checkInnerStreamThreadIds()} method, which
     * determines if an inner stream operation runs in parallel or
     * sequentially.
     */
    public static final int sIntermediateResult = 0;
    public static final int sSequentialResult = 1;
    public static final int sParallelResult = 2;

    /**
     * Determine and print how many threads are used to process an
     * inner stream.  If the inner stream runs sequentially its
     * threadId will match the {@code outerThreadId}.  If the inner
     * stream runs in parallel, then its threadId may not match the
     * {@code outerThreadId}.
     *
     * @param maxIterations The max number of iterations
     * @param innerThreadIdCounter Keep track of whether the max
     *                             number of iterations has been met
     * @param threadMap A {@link Map} that records which threads are
     *                  used by the inner parallel stream
     * @param outerThreadId The outer stream's thread id
     * @return An {@code int} that's either {@code
     *         sIntermediateResult}, {@code sSequentialResult}, or
     *         {@code sParallelResult}, depending on the observed
     *         behavior
     */
    public static int checkInnerStreamThreadIds
        (int maxIterations,
         AtomicInteger innerThreadIdCounter,
         Map<Long, AtomicInteger> threadMap,
         long outerThreadId) {
        // Try to find the AtomicInteger associated with current
        // thread id in the map.
        var value = threadMap
            // If it's the first time in make an initial value of 1.
            .putIfAbsent(Thread.currentThread().threadId(),
                         new AtomicInteger(1));

        // If it's not the first time in increment the AtomicInteger
        // by 1 to indicate this inner thread processed it.
        if (value != null)
            value.incrementAndGet();

        // Conduct analysis when the final iteration is reached.
        if (innerThreadIdCounter.incrementAndGet() == maxIterations) {
            // See if any computations ran in non-outer threads.  If
            // there's only one thread in the map, and it has the
            // outerThreadId, then there was no parallel processing.
            boolean wasSequential = threadMap.size() == 1
                && threadMap.containsKey(outerThreadId);

            Options.display("inner thread ids for outer thread "
                            + outerThreadId
                            + " = "
                            + threadMap
                            + " suggests "
                            + (wasSequential
                               ? "sequential execution"
                               : "parallel execution"));

            // Indicate if the inner stream ran sequentially or in
            // parallel.
            return wasSequential ? sSequentialResult : sParallelResult;
        } else
            // The caller ignores intermediate results.
            return sIntermediateResult;
    }
}
