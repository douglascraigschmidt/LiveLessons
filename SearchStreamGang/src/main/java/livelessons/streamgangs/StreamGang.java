package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Defines a framework for initiating Streams that process input from
 * a generic List of elements E for one or more cycles.
 */
public abstract class StreamGang<E>
       implements Runnable {
    /**
     * Debugging tag that indicates the name of each subclass.
     */
    protected String TAG = this.getClass().getName();

    /**
     * The input List that's processed, which can be initialized via
     * the {@code makeInputList()} factory method.
     */
    protected volatile List<E> mInput;

    /**
     * Executes submitted runnable tasks in a thread pool.
     */
    private Executor mExecutor;

    /**
     * Keeps track of which cycle is currently active.
     */
    private final AtomicLong mCurrentCycle = new AtomicLong(1);

    /**
     * Get the List to use as input.
     */
    protected List<E> getInput() {
        return mInput;
    }

    /**
     * Set the List to use as input and also return it.
     */
    protected List<E> setInput(List<E> input) {
        return mInput = input;
    }

    /**
     * Set the Executor to use to submit/run tasks.
     */
    protected void setExecutor(Executor executor) {
        mExecutor = executor;
    }

    /**
     * Get the Executor to use to submit/run tasks.
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
     * Factory method that makes the next List of input to be
     * processed by the gang of Tasks.
     */
    protected abstract List<E> getNextInput();

    /**
     * Hook method that starts the StreamGang processing.
     */
    protected abstract void initiateStream();

    /**
     * Hook method that can be used as an exit barrier to wait for the gang of
     * tasks to exit.
     */
    protected void awaitTasksDone() {}

    /**
     * Template method that initiates all the tasks in the StreamGang
     * and waits for them to complete.
     */
    @Override
    public void run() {
        // Invoke hook method to get initial List of input data to
        // process.
        if (setInput(getNextInput()) != null) {
            // Invoke hook method to initiate stream gang processing.
            initiateStream();

            // Invoke hook method to wait for all the tasks to exit.
            awaitTasksDone();
        }
    }
}
