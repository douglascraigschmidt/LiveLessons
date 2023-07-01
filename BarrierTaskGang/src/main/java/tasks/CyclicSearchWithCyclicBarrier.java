package tasks;

import java.util.concurrent.CyclicBarrier;

import static utils.Options.printDebugging;

/**
 * Customizes the {@link SearchTaskGangCommon} framework with a {@link
 * CyclicBarrier} to define a test that continues searching a fixed
 * number of input strings via a fixed number of threads until there's
 * no more input to process.
 */
public class CyclicSearchWithCyclicBarrier
    extends SearchTaskGangCommonCyclic {
    /**
     * The barrier that's used to coordinate each cycle, i.e., each
     * Thread must wait on mCyclicBarrier for all the other Threads to
     * complete their processing before they all attempt to move to
     * the next iteration cycle en masse.
     */
    protected CyclicBarrier mCyclicBarrier;

    /**
     * Constructor initializes the data members and superclass.
     */
    public CyclicSearchWithCyclicBarrier(String[] wordsToFind,
                                         String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
            stringsToSearch);
    }

    /**
     * Each thread in the gang uses a call to {@link CyclicBarrier}
     * {@code await()} to wait for all other threads to complete their
     * current cycle.
     */
    @Override
    protected void taskDone(int index)
        throws IndexOutOfBoundsException {
        try {
            // Wait for all other Threads to reach this barrier.
            mCyclicBarrier.await();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hook method invoked by {@code initiateTaskGang()} to perform
     * custom initializations before threads in the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        // Create a CyclicBarrier whose (1) "parties" count
        // corresponds to each element in the input List and (2)
        // barrier action gets the next List of input data (if any).
        mCyclicBarrier = new CyclicBarrier
            (size,
                // Initialize the barrier action.
                () -> {
                    setInput(getNextInput());
                    if (getInput() != null)
                        printDebugging(">>> Started cycle "
                            + currentCycle()
                            + " with "
                            + size
                            + " Thread"
                            + (size == 1 ? "" : "s")
                            + " <<<");
                });

        printDebugging(">>> Started cycle "
            + currentCycle()
            + " with "
            + size
            + " Thread"
            + (size == 1 ? "" : "s")
            + " <<<");
    }
}

