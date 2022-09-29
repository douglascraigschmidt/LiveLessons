import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

/**
 * @class CyclicSearchWithPhaser
 *
 * @brief Customizes the SearchTaskGangCommon framework with a Phaser
 *        to continue searching a variable number of words/Threads
 *        concurrently until there's no more input to process.
 */
public class CyclicSearchWithPhaser 
             extends SearchTaskGangCommonCyclic {
    /**
     * The barrier that's used to coordinate each cycle, i.e., each
     * Thread must await on mPhaser for all the other Threads to
     * complete their processing before they all attempt to move to
     * the next cycle en masse.
     */
    protected Phaser mPhaser;

    /**
     * Indicate that the size of the input List has changed, which
     * requires a reconfiguration to add or remove Threads from the
     * gang of tasks.
     */
    volatile int mReconfiguration;

    /**
     * Synchronizes all Threads during a reconfiguration.
     */
    volatile CyclicBarrier mReconfigurationCyclicBarrier;

    /**
     * Constructor initializes the data members and superclass.
     */
    CyclicSearchWithPhaser(String[] wordsToFind,
                           String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
        mReconfiguration = 0;

        // Create a Phaser that controls how Threads in the task gang
        // synchronize on a dynamically reconfigurable barrier.  The
        // number of Threads may vary on each iteration cycle,
        // depending on the number of Strings provided as input during
        // that cycle.
        mPhaser = new Phaser() {
            // Hook method that performs actions prior to an impending
            // phase advance.
            protected boolean onAdvance(int phase,
                                        int registeredParties) {
                // Record the size of the previous input List to see
                // if a reconfiguration is needed or not.
                int prevSize = getInput().size();

                // Get the new input Strings to process.
                setInput(getNextInput());

                // Bail out if there's no input or no registered
                // parties.
                if (getInput() == null || registeredParties == 0)
                    return true;
                else {
                    int newSize = getInput().size();

                    // See if we need to reconfigure the Phaser due to
                    // changes in the size of the input List.
                    mReconfiguration = newSize - prevSize;

                    // No reconfiguration needed since there was no
                    // change to the size of the input List.
                    if (mReconfiguration == 0) 
                        // Had to add 'TaskGangBarrierTest.' because
                        // printDebugging(string) is not part of the
                        // ancestry of this class
                        BarrierTaskGangTest.printDebugging("@@@@@ Started cycle "
                                                           + mCurrentCycle.get()
                                                           + " with same # of Threads ("
                                                           + newSize
                                                           + ") @@@@@");

                    // A reconfiguration is needed since the size of
                    // the input List changed.
                    else {
                        BarrierTaskGangTest.printDebugging("@@@@@ Started cycle "
                                                           + mCurrentCycle.get()
                                                           + " with "
                                                           + newSize
                                                           + " vs "
                                                           + prevSize
                                                           + " Threads @@@@@");

                        // Create a new CyclicBarrier to manage the
                        // reconfiguration.  We can use a
                        // CyclicBarrier here since there are a fixed
                        // number of Threads involved.
                        mReconfigurationCyclicBarrier = new CyclicBarrier
                            (prevSize,
                             // Create the barrier action.
                             new Runnable() {
                                 public void run() {
                                     // If there are more elements in
                                     // the input List than last time
                                     // create/run new worker Threads
                                     // to process them.
                                     if (prevSize < newSize)
                                         for (int i = prevSize; i < newSize; ++i)
                                             new Thread(makeTask(i)).start();

                                     // Indicate that reconfiguration
                                     // is done.
                                     mReconfiguration = 0;
                                 }
                             });
                    }
                    return false;
                }
            }
        };
    }

    /**
     * Factory method that creates a Runnable task that processes one
     * element of the input List (at location @code index) in a
     * background Thread.
     */
    @Override
    protected Runnable makeTask(final int index) {
        // Register ourselves with the Phaser so we're included in the
        // set of registered parties.
        mPhaser.register();

        // Forward the rest of the processing to the superclass's
        // makeTask() factory method.
        return super.makeTask(index);
    }

    /**
     * Hook method called back by initiateTaskGang() to perform custom
     * initializations before the Threads in the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        // Print diagnostic information.
        BarrierTaskGangTest.printDebugging
            ("@@@@@ Started cycle 1 with "
             + size
             + " Thread"
             + (size == 1 ? "" : "s")
             + " @@@@@");
    }

    /**
     * Each Thread in the gang uses the Phaser arriveAndAwaitAdvance()
     * method to wait for all the other Threads to complete their
     * current cycle.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        boolean throwException = false;
        try {
            // Wait until all other Threads are done with their
            // iteration cycle before attempting to advance.
            mPhaser.arriveAndAwaitAdvance();

            // Check to see if a reconfiguration is needed.
            if (mReconfiguration != 0) {
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

                        // Indicate that we need to throw the
                        // IndexoutOfBoundsException so this Thread
                        // will be stopped.
                        throwException = true;
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } 
            }                    
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 

        // Throw this exception, which triggers the worker Thread
        // to exit.
        if (throwException)
            throw new IndexOutOfBoundsException();                
    }
}

