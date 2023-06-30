package utils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static utils.Options.print;
import static utils.Options.printDebugging;

/**
 * Defines a framework for spawning and running a "gang" of tasks that
 * concurrently process input from a generic {@link List} of elements
 * {@code E} for one or more cycles.
 */
public abstract class TaskGang<E>
       implements Runnable {
    /**
     * The input {@link List} that's processed, which can be
     * initialized via the {@code setInput()} method.
     */
    private volatile List<E> mInput;

    /**
     * Executes submitted Runnable tasks in a {@link Thread} pool.
     */
    private Executor mExecutor;

    /**
     * Keeps track of which cycle is currently active.
     */
    private final AtomicLong mCurrentCycle = new AtomicLong(0);

    /**
     * Get the List to use as input.
     */
    protected List<E> getInput() {
        return mInput;
    }

    /**
     * Set the {@link List} to use as input and also return it.
     */
    protected List<E> setInput(List<E> input) {
        return mInput = input;
    }

    /**
     * Set the {@link Executor} to use to submit/run tasks.
     */
    protected void setExecutor(Executor executor) {
        mExecutor = executor;
    }

    /**
     * Get the {@link Executor} to use to submit/run tasks.
     */
    protected Executor getExecutor() {
        return mExecutor;
    }

    /**
     * Increment to the next cycle.
     */
    @SuppressWarnings("UnusedReturnValue")
    protected long incrementCycle() {
        return mCurrentCycle.incrementAndGet();
    }

    /**
     * Return the current cycle.
     */
    protected long currentCycle() {
        return mCurrentCycle.get();
    }

    /**
     * Factory method that makes the next {@link List} of input to be
     * processed concurrently by the gang of tasks (must be overridden
     * by subclasses).
     */
    protected abstract List<E> getNextInput();

    /**
     * Hook method called back by {@code initiateTaskGang()} to enable
     * subclasses to perform custom initializations before the tasks
     * in the gang are spawned.
     */
    protected void initiateHook(int inputSize) {
        // No-op by default.
    }

    /**
     * Initiate the {@link TaskGang} (must be overridden by
     * subclasses).
     */
    protected abstract void initiateTaskGang(int inputSize);

    /**
     * Hook method that returns true as long as the task processing
     * should continue. By default, returns false, which means a
     * {@link TaskGang} will be only "one-shot" unless this method is
     * overridden.
     */
    protected boolean advanceTaskToNextCycle() {
        return false;
    }

    /**
     * Hook method that can be used as an exit barrier to wait for the
     * gang of tasks to exit (must be overridden by subclasses).
     */
    protected abstract void awaitTasksDone();

    /**
     * Hook method called when a task is done. Can be used in
     * conjunction with a one-shop or cyclic barrier to wait for all
     * the other tasks to complete their current cycle. It's passed
     * the index of the work that's done. Throws the
     * {@link IndexOutOfBoundsException} if the item has been removed.
     */
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        // No-op.
    }

    /**
     * Hook method that performs work a background task. Returns true
     * if all goes well, else false to stop the background task
     * from continuing to run (must be overridden by subclasses).
     */
    protected abstract boolean processInput(E inputData);

    /**
     * Template method that creates/executes all the tasks in the
     * gang.
     */
    @Override
    public void run() {
        // Invoke hook method to get initial List of input data to
        // process.
        if (setInput(getNextInput()) != null) {
            // Invoke hook method to initiate the gang of tasks.
            initiateTaskGang(getInput().size());

            // Invoke hook method to wait for all the tasks to exit
            // (this call runs concurrently wrt the gang of tasks).
            awaitTasksDone();
        }            
    }

    /**
     * @return A {@link Runnable} task that processes an element of the
     *         input List at {@code index} in a background task provided
     *         by the {@link Executor} returned by {@code getExecutor()}
     */
    protected Runnable makeTask(final int index) {
        // This lambda runs in a background task provided by the
        // Executor.
        return () -> {
            try {
                // Get the input data element associated with
                // this index.
                E element = getInput().get(index);

                // Process input data element.
                if (processInput(element))
                    // Success indicates the worker task is done with
                    // this cycle.
                    taskDone(index);
            } catch (IndexOutOfBoundsException ignored) {
            }
        };
    }
}
