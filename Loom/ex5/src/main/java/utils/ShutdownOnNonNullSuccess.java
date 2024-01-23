package utils;

import java.util.concurrent.StructuredTaskScope;

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
     * @param subtask the completed task
     */
    @Override
    protected void handleComplete(Subtask<? extends T> subtask) {
        var state = subtask.state();
        if (state == StructuredTaskScope.Subtask.State.UNAVAILABLE) {
            throw new IllegalArgumentException("Task is not completed");
        } else if (state == StructuredTaskScope.Subtask.State.SUCCESS) {
            // Get the result of the Future.
            T result = subtask.get();

            // The first non-null result is stored and the scope is
            // shutdown.
            if (result != null) {
                mResult = result;
                shutdown();
            }
        }
    }

    /**
     * {@inheritDoc}
     * @return this task scope
     * @throws IllegalStateException {@inheritDoc}
     * @throws WrongThreadException {@inheritDoc}
     */
    @Override
    public ShutdownOnNonNullSuccess<T> join()
        throws InterruptedException {
        super.join();
        return this;
    }

    /**
     * @return The first computation to match or null if there are no
     *         matches
     */
    public T result() {
        return mResult;
    }
}
