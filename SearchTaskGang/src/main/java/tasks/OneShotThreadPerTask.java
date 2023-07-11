package tasks;

import utils.SearchResults;
import utils.TaskGang;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import static utils.ExceptionUtils.rethrowRunnable;

/**
 * Customizes the {@link SearchTaskGangCommon} framework to process a
 * one-shot {@link List} of tasks via an {@link Executor} that creates
 * a virtual {@link Thread} for each task. The {@link Executor} model
 * uses a virtual {@link Thread}-per-task. The unit of concurrency is
 * each input {@link String} and the results processing model is
 * synchronous.
 */
public class OneShotThreadPerTask
       extends SearchTaskGangCommon {
    /**
     * The {@link List} of worker {@link Thread} objects that were
     * created.
     */
    private final List<Thread> mWorkerThreads;

    /**
     * Constructor initializes the superclass and data members.
     */
    public OneShotThreadPerTask(String[] wordsToFind,
                                String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);

        // This List holds Thread objects so they can be joined.
        mWorkerThreads = new LinkedList<>();
    }

    /**
     * Initiate the {@link TaskGang} to run each task in a separate
     * {@link Thread}.
     */
    protected void initiateTaskGang(int inputSize) {
        // Create thread to run each task.
        if (getExecutor() == null) 
            // Create an Executor that runs each worker task in a
            // separate virtual Thread.
            setExecutor (runnable -> mWorkerThreads
                .add(Thread.startVirtualThread(runnable))
            );

        // Enqueue each item in the input List for execution in a
        // separate Thread.
        for (int i = 0; i < inputSize; ++i) 
            getExecutor().execute(makeTask(i));
    }

    /**
     * Runs in a background {@link Thread} and searches the {@code
     * inputData} for all occurrences of words to find.
     */
    @Override
    protected boolean processInput (String inputData) {
        // Iterate through each word we're searching for.
        for (String word : mWordsToFind) {
            // Try to find the word in the inputData.
            var results = searchForWord(word, inputData);

            // Each time a match is found, the SearchResult.print()
            // method is called to print the output.  We put this call
            // in a synchronized block so the output isn't scrambled.
            synchronized(System.out) {
                results.print();
            }
        }

        return true;
    }

    /**
     * This hook method uses {@link Thread#join()} as an exit barrier
     * to wait for the gang of tasks to exit.
     */
    protected void awaitTasksDone() {
        // Iterate through all the Thread objects and join them.
        for (Thread thread : mWorkerThreads)
            rethrowRunnable(thread::join);
    }
}

