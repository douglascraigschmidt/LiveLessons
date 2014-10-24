import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @class CyclicExecutorService
 *
 * @brief Customizes the SearchTaskGangCommon framework to process
 *        a cyclic List of tasks via a pool of Threads created by
 *        the Executor, which is also used to wait for all
 *        the Threads in the pool to shutdown.
 */
public class CyclicExecutorService
       extends OneShotExecutorService {
    /**
     * Constructor initializes the superclass.
     */
    CyclicExecutorService(String[] wordsToFind,
                          String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Initiate the TaskGang to run each worker in the Thread
     * pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Allow subclasses to customize their behavior before the
        // Threads in the gang are spawned.
        initiateHook(inputSize);

        // Create a new collection that will contain all the
        // Worker Runnables.
        List<Callable<Object>> workerCollection =
            new ArrayList<Callable<Object>>(inputSize);

        // Create a Runnable for each item in the input List and
        // add it as a Callable adapter into the collection.
        for (int i = 0; i < inputSize; ++i) 
            workerCollection.add(Executors.callable(makeTask(i)));

        try {
            ExecutorService executorService = 
                (ExecutorService) getExecutor();

            // Use invokeAll() to execute all items in the collection
            // via the Executor's Thread pool.
            executorService.invokeAll(workerCollection);
        } catch (InterruptedException e) {
            System.out.println("invokeAll() interrupted");
        }
    }

    /**
     * When there's no more input data to process release the exit
     * latch and return false so the worker Thread will return.
     * Otherwise, return true so the worker Thread will continue
     * to run.
     */
    @Override
        protected boolean advanceTaskToNextCycle() {
        if (setInput(getNextInput()) == null) 
            return false;
        else {
            // Invoke method to initialize the gang of Threads.
            initiateTaskGang(getInput().size());

            return true;
        }
    }
}

