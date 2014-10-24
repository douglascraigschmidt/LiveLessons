import java.util.Vector;

/**
 * @class ThreadGang
 *
 * @brief Defines a framework for spawning and running a "gang" of
 *        Threads that concurrently process input from a generic
 *        Vector of elements E for one or more cycles.
 */
public abstract class ThreadGang<E, R> implements Runnable {
    /**
     * The input Vector that's processed, which can be initialized via
     * the @code makeInputList() factory method.
     */
    protected volatile Vector<E> mInput = null;

    /**
     * Set the Vector to use as input.
     */
    protected void setVector(Vector<E> input) {
        mInput = input;
    }

    /**
     * Get the Vector to use as input.
     */
    protected Vector<E> getVector() {
        return mInput;
    }

    /**
     * Factory method that makes the next Vector of input to be
     * processed concurrently by the gang of Threads.
     */
    protected abstract Vector<E> getNextInput();

    /**
     * Hook method that initiates the gang of Threads.
     */
    protected abstract void initiateThreadGang(int inputSize);

    /**
     * Hook method that returns true as long as the processing should
     * continue.  By default, returns false, which means a ThreadGang
     * will be only "one-shot" this method is overridden.
     */
    protected boolean advanceToNextCycle() {
        return false;
    }

    /**
     * Hook method that can be used as an exit barrier to wait for the
     * gang of Threads to exit.
     */
    protected abstract void awaitThreadGangDone();

    /**
     * Hook method called when a worker Thread is done.  Can be used
     * in conjunction with a one-shop or cyclic barrier to wait for
     * all the other Threads to complete their current cycle.  Is
     * passed the index of the work that's done.  Returns true if the
     * wait was successfuly or throws the IndexOutOfBoundsException if
     * the item has been removed.
     */
    protected void workerDone(int index) throws IndexOutOfBoundsException {
        // No-op.
    }
    
    /**
     * Hook method that performs work a background Thread.  Returns
     * true if all goes well, else false (which will stop the
     * background Thread from continuing to run).
     */
    public abstract boolean doWorkInBackground(E inputData);

    /**
     * Hook method that can be used by doWorkInBackground() to process
     * results.
     */
    protected abstract void processResults(R results);

    /**
     * Template method that creates/runs all the Threads in the gang.  
     */
    @Override
    public void run() {
        // Invoke hook method to get initial Vector of input data to
        // process.
        setVector (getNextInput());

        // Invoke hook method to initialize the gang of Threads.
        initiateThreadGang(getVector().size());

        // Invoke hook method to wait for all the Threads to exit.
        awaitThreadGangDone();
    }

    /**
     * Factory method that creates a Runnable worker that will process
     * one node of the input Vector (at location @code index) in a
     * background Thread.
     */
    protected Runnable makeWorker(final int index) {
        return new Runnable() {

            // This method runs in background Thread.
            public void run() {

                do {
                    try {
                        // Get the input data element associated with
                        // this index.
                        E element = getVector().get(index);

                        // Process input data element.
                        if (doWorkInBackground(element) == false)
                            return;
                        else
                            // Indicate the worker Thread is done with
                            // this cycle, which can block on an exit
                            // barrier.
                            workerDone(index);
                    } catch (IndexOutOfBoundsException e) {
                            return;
                    }

                    // Keep running until instructed to stop.
                } while (advanceToNextCycle());
            }
        };
    }
}
