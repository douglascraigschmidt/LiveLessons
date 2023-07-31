package tasks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static utils.Options.printDebugging;

/**
 * Leverages the {@link SearchTaskGangCommon}'s ability to spawn a
 * virtual {@link Thread} for each element in the {@link List} of
 * input strings.  It also customizes this super class to use a {@link
 * CountDownLatch} that waits for all {@link Thread} objects to finish
 * concurrently searching a {@link List} of input {@link String} objects
 * to determine if there are any matches from an array of words to
 * find. This class only runs for a single iteration cycle.
 */
public class OneShotSearchWithCountDownLatch 
             extends SearchTaskGangCommon {
    /**
     * Constructor initializes the super class.
     */
    public OneShotSearchWithCountDownLatch
        (String[] wordsToFind,
         String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);
    }

    /**
     * Hook method invoked by {@code initiateTaskGang()} to perform
     * custom initializations before tasks in the gang are spawned.
     */
    @Override
    protected void initiateHook(int size) {
        printDebugging
            (">>> Started cycle 1 with "
             + size
             + " Thread"
             + (size == 1 ? "" : "s")
             + " <<<");

        // Create a CountDownLatch whose count corresponds to each
        // Thread and element in the input List (which have the same
        // value since this model is "Thread-per-input-element").
        mExitBarrier = new CountDownLatch(size);
    }

    /**
     * Hook method called when a worker {@link Thread} is done.
     */
    @Override
    protected void taskDone(int index)
        throws IndexOutOfBoundsException {
        // Decrement the CountDownLatch by one.  When the count
        // reaches 0 the main Thread is released from its call to
        // await().
        mExitBarrier.countDown();
    }
}

