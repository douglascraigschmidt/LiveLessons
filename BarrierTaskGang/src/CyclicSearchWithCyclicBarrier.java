import java.util.concurrent.CyclicBarrier;

/**
 * @class CyclicSearchWithCyclicBarrier
 *
 * @brief Customizes the SearchTaskGangCommon framework with a
 *        CyclicBarrier to continue searching a fixed number of
 *        input Strings via a fixed number of Threads until
 *        there's no more input to process.
 */
public class CyclicSearchWithCyclicBarrier 
              extends SearchTaskGangCommonCyclic {
    /**
     * The barrier that's used to coordinate each cycle, i.e., each
     * Thread must await on mCyclicBarrier for all the other Threads
     * to complete their processing before they all attempt to move to
     * the next iteration cycle en masse.
     */
    protected CyclicBarrier mCyclicBarrier;

    /**
     * Constructor initializes the data members and superclass.
     */
    CyclicSearchWithCyclicBarrier(String[] wordsToFind,
                                  String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Each Thread in the gang uses a call to CyclicBarrier await() to
     * wait for all the other Threads to complete their current cycle.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        try {
            // Wait for all other Threads to reach this barrier.
            mCyclicBarrier.await();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }

    /**
     * Hook method called back by initiateTaskGang() to perform custom
     * initializations before the Threads in the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        // Create a CyclicBarrier whose (1) "parties" count
        // corresponds to each element in the input List and (2)
        // barrier action gets the next List of input data (if any).
        mCyclicBarrier = new CyclicBarrier
            (size,
             new Runnable() {
                 public void run() {
                     setInput(getNextInput());
                     if (getInput() != null)
                         BarrierTaskGangTest.printDebugging
                             ("@@@@@ Started cycle "
                              + currentCycle()
                              + " @@@@@");
                 }
             });
        BarrierTaskGangTest.printDebugging
            ("@@@@@ Started cycle 1 with "
             + size
             + " Thread"
             + (size == 1 ? "" : "s")
             + " @@@@@");
    }
}

