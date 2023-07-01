package tasks;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

import static utils.Options.print;
import static utils.Options.printDebugging;

/**
 * Customizes the {@link SearchTaskGangCommon} framework with a {@link
 * Phaser} to continue searching a variable number of words/threads
 * concurrently until there's no more input to process.
 */
public class CyclicSearchWithPhaser 
             extends SearchTaskGangCommonCyclic {
    /**
     * The barrier that's used to coordinate each cycle, i.e., each
     * Thread must await on mPhaser for all the other threads to
     * complete their processing before they all attempt to move to
     * the next cycle en masse.
     */
    protected Phaser mPhaser;

    /**
     * Indicate that the size of the input List has changed, which
     * triggers a reconfiguration to add or remove threads from the
     * gang of tasks.  The volatile type qualifier ensures threads
     * atomically read from and write to this field.
     */
    volatile boolean mReconfiguration;

    /**
     * Synchronizes all threads when a reconfiguration is triggered.
     */
    volatile CyclicBarrier mReconfigurationCyclicBarrier;

    /**
     * Constructor initializes the data members and superclass.
     */
    public CyclicSearchWithPhaser(String[] wordsToFind,
                                  String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);

        // No reconfiguration needed initially.
        mReconfiguration = false;

        // Initialize the Phaser.
        mPhaser = makePhaser();
    }

    /**
     * Create a {@link Phaser} that controls how threads in the task
     * gang synchronize on a dynamically reconfigurable barrier.  The
     * number of threads may vary for each iteration cycle, depending
     * on the number of String objects provided as input during that
     * cycle.
     *
     * @return A {@link Phaser} whose {@code onAdvance()} hook method
     *         creates a {@link CyclicBarrier} to handle
     *         reconfiguration.
     */
    private Phaser makePhaser() {
        return new Phaser() {
            /**
             * Hook method that performs actions prior to an impending
             * phase advance.
             *
             * @param phase The current phase number on entry to this
             *              method, before this phaser is advanced
             * @param registeredParties The current number of
             *                          registered parties
             * @return True if the {@link Phaser} should terminate,
             *         else false
             */
            @Override
            public boolean onAdvance(int phase,
                                     int registeredParties) {
                // Record the size of the previous input List to see
                // if a reconfiguration is needed or not.
                int prevSize = getInput().size();

                // Get the new input Strings to process.
                setInput(getNextInput());

                // Bail out if there's no more input.
                if (getInput() == null)
                    return true;
                else {
                    int newSize = getInput().size();

                    // See if we need to reconfigure the Phaser due to
                    // changes in the size of the input List.
                    mReconfiguration = newSize - prevSize != 0;

                    // No reconfiguration needed since there was no
                    // change to the size of the input List.
                    if (!mReconfiguration)
                        printDebugging(">>> Started cycle "
                                       + currentCycle()
                                       + " with same number ("
                                       + newSize
                                       + ") of threads <<<");

                    // A reconfiguration is needed since the size of
                    // the input List changed.
                    else {
                        printDebugging(">>> Started cycle "
                                       + currentCycle()
                                       + " with "
                                       + newSize
                                       + " vs "
                                       + prevSize
                                       + " threads <<<");

                        // Manage the reconfiguration via a new
                        // CyclicBarrier since there are a fixed
                        // number of new threads involved.
                        mReconfigurationCyclicBarrier = new CyclicBarrier
                            (prevSize,
                             // Create the barrier action.
                             () -> {
                                // If there are more elements in
                                // the input List than last time,
                                // create/run new worker Threads
                                // to process them.
                                if (prevSize < newSize)
                                    for (int i = prevSize; i < newSize; ++i)
                                        new Thread(makeTask(i)).start();

                                // Indicate that reconfiguration
                                // is done.
                                mReconfiguration = false;
                            });
                    }
                    return false;
                }
            }
        };
    }

    /**
     * @return A {@link Runnable} task that processes one element of
     * the input {@link List} at location {@code index} in a
     * background {@link Thread}.
     */
    @Override
    protected Runnable makeTask(final int index) {
        // Register ourselves with the Phaser, so we're included in
        // the set of registered parties.
        mPhaser.register();

        // Forward the rest of the processing to the superclass
        // makeTask() factory method.
        return super.makeTask(index);
    }

    /**
     * Hook method called back by {@code initiateTaskGang()} to
     * perform any custom initializations needed before the tasks in
     * the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        // Print diagnostic information.
        printDebugging
            (">>> Started cycle 1 with "
             + size
             + " thread"
             + (size == 1 ? "" : "s")
             + " <<<");
    }

    /**
     * Wait for all other tasks to complete their iteration cycle
     * before attempting to advance.  Also handles reconfigurations
     * triggered by a change in the number of threads, if needed.
     */
    @Override
    protected void taskDone(int index)
        throws IndexOutOfBoundsException {
        IndexOutOfBoundsException exception = null;
        try {
            // Each task uses the Phaser arriveAndAwaitAdvance()
            // method to wait for all other tasks to complete their
            // current cycle before advancing.
            mPhaser.arriveAndAwaitAdvance();

            // Check to see if a reconfiguration is needed, which
            // occurs when the number of threads changes between
            // cycles.
            if (mReconfiguration) {
                try {
                    // Wait for all existing threads to reach this
                    // barrier.
                    mReconfigurationCyclicBarrier.await();

                    // Check to see if this Thread is no longer
                    // needed, i.e., if the new input List shrank
                    // relative to the previous input List.
                    if (index >= getInput().size()) {
                        // Remove ourselves from the count of parties
                        // that will wait on this Phaser.
                        mPhaser.arriveAndDeregister();

                        // We must throw the IndexOutOfBoundsException
                        // to stop this Thread from running.
                        exception = new IndexOutOfBoundsException(index);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } 
            }                    
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 

        // Throw this exception, which triggers the calling worker
        // Thread to exit since it's no longer needed.
        if (exception != null) 
            throw exception;
    }
}

