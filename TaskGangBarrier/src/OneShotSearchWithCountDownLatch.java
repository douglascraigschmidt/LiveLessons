import java.util.concurrent.CountDownLatch;

/**
 * @class OneShotSearchWithCountDownLatch
 *
 * @brief Customizes the SearchTaskGangCommon framework to spawn a
 *        Thread for each element in the List of input Strings and
 *        use a CountDownLatch to wait for all the Threads to
 *        finish.
 */
public class OneShotSearchWithCountDownLatch 
              extends SearchTaskGangCommon {
    /**
     * Constructor initializes the super class.
     */
    OneShotSearchWithCountDownLatch(String[] wordsToFind,
                                   String[][] stringsToFind) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToFind);
    }

    /**
     * Hook method called back by initiateTaskGang() to perform
     * custom initializations before the Threads in the gang are
     * spawned.
     */
    @Override
    protected void initiateHook(int size) {
        System.out.println("@@@@@ Started cycle 1 with "
        		           + size
                           + " Thread"
                           + (size == 1 ? "" : "s")
                           + " @@@@@");

        // Create a CountDownLatch whose count corresponds to each
        // Thread and element in the input List (which are
        // identical)
        mExitBarrier = new CountDownLatch(size);
    }

    /**
     * Hook method called when a worker Thread is done - it
     * decrements the CountDownLatch.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        mExitBarrier.countDown();
    }
}

