package tasks;

import tasks.SearchTaskGangCommon;
import utils.TaskGang;

import java.util.concurrent.CountDownLatch;

/**
 * This helper class extends {@link SearchTaskGangCommon} and factors
 * out common code used by the cyclic {@link TaskGang}
 * implementations.
 */
public abstract class SearchTaskGangCommonCyclic
                      extends SearchTaskGangCommon {
    /**
     * Constructor initializes the data members.
     */
    protected SearchTaskGangCommonCyclic
        (String[] wordsToFind,
         String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super (wordsToFind, stringsToSearch);

        // Initialize the exit barrier to 1, which causes
        // awaitTasksDone() hook method to block until the test is
        // finished.
        mExitBarrier = new CountDownLatch(1);
    }

    /**
     * When there's no more input data to process, release the exit
     * latch and return false, so the worker {@link Thread} will
     * return.  Otherwise, return true so the worker {@link Thread}
     * will continue to run.
     */
    @Override
    protected boolean advanceTaskToNextCycle() {
        if (getInput() == null) {
            mExitBarrier.countDown();
            return false;
        } else
            return true;
    }
}
