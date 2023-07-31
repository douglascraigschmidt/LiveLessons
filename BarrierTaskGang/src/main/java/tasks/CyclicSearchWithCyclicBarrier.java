package tasks;

import java.util.concurrent.CyclicBarrier;

import static utils.ExceptionUtils.rethrowSupplier;
import static utils.Options.printDebugging;

/**
 * Customizes the {@link SearchTaskGangCommonCyclic} super class with a
 * {@link CyclicBarrier} to define a test that continues searching a fixed
 * number of input strings via a fixed number of {@link Thread} object
 * until there's no more input to process.
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
     *
     * @param wordsToFind The array of words to find
     * @param stringsToSearch The array of strings to search
     */
    public CyclicSearchWithCyclicBarrier(String[] wordsToFind,
                                         String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);
    }

    /**
     * Hook method invoked by {@code initiateTaskGang()} to perform
     * custom initializations before threads in the gang are spawned.
     *
     * @param size The size of the task gang.
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

    /**
     * Each thread in the gang uses a call to {@link CyclicBarrier}
     * {@code await()} to wait for all other threads to complete their
     * current cycle.
     *
     * @param index The index of the current Thread in the gang.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (this method doesn't use it, but it's declared to
     *         conform to the {@code TaskGang})
     */
    @Override
    protected void taskDone(int index)
        throws IndexOutOfBoundsException {
        // Wait for all other Threads to reach this barrier.
        rethrowSupplier(mCyclicBarrier::await).get();
    }
}

