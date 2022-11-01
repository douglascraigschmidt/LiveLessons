import java.util.concurrent.CountDownLatch;

/**
 * @class OneShotSearchWithCountDownLatch
 *
 * @brief Customizes the SearchTaskGangCommon framework to spawn a
 *        Thread for each element in the List of input Strings and
 *        use a CountDownLatch to wait for all the Threads to
 *        finish concurrently searching the input for an array of 
 *        words to find. It only runs for a single iteration cycle.
 */
public class OneShotSearchWithCountDownLatch 
              extends SearchTaskGangCommon {
    /**
     * Constructor initializes the super class.
     */
    OneShotSearchWithCountDownLatch(String[] wordsToFind,
                                   String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Hook method invoked by initiateTaskGang() to perform custom
     * initializations before the Threads in the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        BarrierTaskGangTest.printDebugging
            ("@@@@@ Started cycle 1 with "
             + size
             + " Thread"
             + (size == 1 ? "" : "s")
             + " @@@@@");

        // Create a CountDownLatch whose count corresponds to each
        // Thread and element in the input List (which have the same
        // value since this model is "Thread-per-input-element").
        mExitBarrier = new CountDownLatch(size);
    }

    /**
     * Hook method called when a worker Thread is done.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        // Decrement the CountDownLatch by one.  When the count
        // reaches 0 the main Thread is released from its call to
        // await().
        mExitBarrier.countDown();
    }
}

