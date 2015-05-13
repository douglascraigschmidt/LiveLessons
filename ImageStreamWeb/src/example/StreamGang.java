package example;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @class StreamGang
 *
 * @brief Defines a framework for spawning and running a "gang" of
 *        streams that concurrently process input from a generic List
 *        of elements E for one or more cycles.
 */
public abstract class StreamGang<E> implements Runnable {
    /**
     * The input List that's processed, which can be initialized via
     * the @code makeInputList() factory method.
     */
    private volatile List<E> mInput = null;

    /**
     * Executes submitted Runnable tasks in a Thread pool.
     */
    private Executor mExecutor = null;

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
     * Factory method that makes the next List of input to be processed
     * concurrently by the gang of Tasks.
     */
    protected abstract List<E> getNextInput();

    /**
     * Initiate the StreamGang processing
     */
    protected abstract void initiateStream();

    /**
     * Hook method that can be used as an exit barrier to wait for the gang of
     * tasks to exit.
     */
    protected abstract void awaitTasksDone();

    /**
     * Template method that creates/executes all the tasks in the
     * gang.
     */
    @Override
    public void run() {
        // Invoke hook method to get initial List of input data to
        // process.
        if (setInput(getNextInput()) != null) {
            // Invoke hook method to initialize the gang of tasks.
            initiateStream();

            // Invoke hook method to wait for all the tasks to exit.
            awaitTasksDone();
        }            
    }
}
