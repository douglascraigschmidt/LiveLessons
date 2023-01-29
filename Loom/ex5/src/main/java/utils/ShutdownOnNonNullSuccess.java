package utils;

import jdk.incubator.concurrent.StructuredTaskScope;

import java.util.concurrent.Future;

/**
 * A {@link StructuredTaskScope} that captures the result of the first
 * subtask to complete successfully (i.e., without returning a {@code
 * null}) or returns {@code null} if no subtask completes
 * successfully.  Once captured, it invokes the {@code shutdown()}
 * method to interrupt unfinished threads and wakeup the owner.
 *
 * The policy implemented by this class is intended for cases where
 * the result of any subtask will do ("invoke any") and where the
 * results of other unfinished subtask are no longer needed.
 */
public class ShutdownOnNonNullSuccess<T>
       extends StructuredTaskScope<T> {
    /**
     * Stores the first computation to match or null if there are no
     * matches.
     */
    private volatile T mResult;

    /**
     * Creates an unnamed structured task scope that creates virtual
     * threads.
     */
    public ShutdownOnNonNullSuccess() {
        super(null,
              Thread.ofVirtual().factory());
    }

    /**
     * Invoked when a task completes before the scope is shut down.
     * This method may be invoked by several threads concurrently.
     *
     * @param future the completed task
     */
    @Override
    protected void handleComplete(Future<T> future) {
        Future.State state = future.state();
        if (state == Future.State.RUNNING) {
            throw new IllegalArgumentException("Task is not completed");
        } else if (future.state() == Future.State.SUCCESS) {
            // Get the result of the Future.
            T result = future.resultNow();

            // The first non-null result is stored and the scope is
            // shutdown.
            if (result != null) {
                mResult = result;
                shutdown();
            }
        }
    }

    /**
     * @return The first computation to match or null if there are no
     *         matches
     */
    public T result() {
        return mResult;
    }
}
