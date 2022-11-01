package livelessons.streams;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Defines a framework for initiating Streams that process input from
 * a generic List of elements E for one or more cycles.  This class
 * plays the role of an "Abstract Class" in the Template Method
 * pattern.
 */
public abstract class StreamGang<E> 
       implements Runnable {
    /**
     * Debugging tag
     */
    protected String TAG = this.getClass().getName();

    /**
     * Template method that initiates the stream processing.
     */
    @Override
    public void run() {
        // Invoke hook method to get initial List of input data to
        // process.
        if (setInput(getNextInput()) != null) {
            // Invoke hook method to start the Stream processing.
            initiateStream();

            // Invoke hook method to wait for all stream processing
            // tasks to finish.
            awaitTasksDone();
        }            
    }

    /**
     * Factory method that makes the next List of input to be processed
     * concurrently by the gang of Tasks.
     */
    protected abstract List<E> getNextInput();

    /**
     * Initiate the StreamGang processing.
     */
    protected abstract void initiateStream();

    /**
     * Hook method that can be used as an exit barrier to wait for the gang of
     * tasks to exit.
     */
    protected abstract void awaitTasksDone();

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
    protected long incrementCycle() {
        return mCurrentCycle.incrementAndGet();
    }

    /**
     * Return the current cycle.
     */
    protected long currentCycle() {
        return mCurrentCycle.get();
    }
}
