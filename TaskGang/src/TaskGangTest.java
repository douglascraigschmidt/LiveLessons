import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

/**
 * @class TaskGangTest
 *
 * @brief This program tests various subclassses of the TaskGang
 *        framework, which use different Java concurrency and
 *        synchronization mechanisms to implement an "embarraassingly
 *        parallel" application that concurrently searches for words
 *        in a List of Strings.
 *
 * @@ NS: Need to improve the documentation.
 */
public class TaskGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        EXECUTOR_ONESHOT,
        EXECUTOR_CYCLIC,
        EXECUTOR_FUTURE_ONESHOT,
        EXECUTOR_COMPLETION_ONESHOT
    }

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true
        ;
    // @@ NS: Need to get this data from files rather than from
    // hard-coded strings!

    /**
     * This input array is used by the one-shot tests that search for
     * the words concurrently in multiple threads.
     */
    private final static String[][] mOneShotInputStrings = {
        {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a fixed number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[][] mFixedNumberOfInputStrings = {
        {"xdoodoo", "xreo", "xmiomio", "xfao", "xsoosoo", "xlao", "xtiotio", "xdoo"},
        {"xdoo", "xreoreo", "xmio", "xfaofao", "xsoo", "xlaolao", "xtio", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a variable number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[][] mVariableNumberOfInputStrings = {
        {"xfaofao"},
        {"xsoo", "xlaolao", "xtio", "xdoodoo"},
        {"xdoo", "xreoreo"},
        {"xreoreo", "xdoo"},
        {"xdoodoo", "xreo", "xmiomio"}
    };

    /**
     * Array of words to search for in the input.
     */
    private final static String[] mWordList = {"do",
                                               "re",
                                               "mi",
                                               "fa",
                                               "so",
                                               "la",
                                               "ti",
                                               "do"};
        
    /**
     * @class SearchResult
     *
     * @brief Holds one search result.
     */
    static public class SearchResult {
        /**
         * The index in the search String where the word that was
         * found.
         */
        public int mIndex;

        /**
         * Create a SearchResult object contains meta-data about a
         * search result..
         */
        public SearchResult(int index) {
            mIndex = index;
        }
    }

    /**
     * @class SearchResults
     *
     * @brief Holds the search results.
     */
    static public class SearchResults implements Iterable<SearchResult> {
        /**
         * Id of the Thread that found a search result.
         */
        public long mThreadId;

        /**
         * The word that was found.
         */
        public String mWord;

        /**
         * The input String used for the search.
         */
        public String mInputData;

        /**
         * The cycle in which the search result was found.
         */
        public long mCycle;

        /**
         * The List of SearchResult objects that matched the @code mWord.
         */
        protected List<SearchResult> mList;
        
        /**
         * Create an empty SearchResults, which is used to shutdown
         * processing of the BlockingQueue.
         */
        public SearchResults() {
            mList = null;
        }
        
        /**
         * Create a SearchResults with values for the various fields.
         */
        public SearchResults(long threadId,
                             long cycle,
                             String word,
                             String inputData) {
            mThreadId = threadId;
            mCycle = cycle;
            mWord = word;
            mInputData = inputData;
            mList = new ArrayList<SearchResult>();
        }
        
        /**
         * Convert to String form.
         */
        public String toString() {
            return 
                "["
                + mThreadId
                + "|"
                + mCycle
                + "] "
                + mWord
                + " at "
                + mInputData;
        }

        /**
         * Add a SearchResult.
         */
        public void add(SearchResult result) {
            mList.add(result);
        }

        /**
         * Returns true if there are no search results.
         */
        public boolean isEmpty() {
            return mList.size() == 0;
        }

        /**
         * Returns an iterator over elements of type @code SearchData;
         *
         * @return an Iterator.
         */
        public Iterator<SearchResult> iterator() {
            return mList.iterator();
        }

        /**
         * Print the results.
         */
        void print() {
            if (!isEmpty()) {
                System.out.print(toString());

                for (SearchResult result : this)
                    System.out.print ("["
                                      + result.mIndex
                                      + "]");
                System.out.println("");
            }
        }
    }

    /**
     * @class SearchTaskGangCommon
     * 
     * @brief This helper class factors out the common code used by
     *        all the implementations of TaskGang below.  It
     *        customizes the TaskGang framework to concurrently search
     *        an array of Strings for an array of words to find.
     */
    static public abstract class SearchTaskGangCommon
        extends TaskGang<String> {
        /**
         * The array of words to find.
         */
        protected final String[] mWordsToFind;

        /**
         * An Iterator for the array of Strings to search.
         */
        private final Iterator<String[]> mInputIterator;

        /**
         * Constructor initializes the data members.
         */
        protected SearchTaskGangCommon(String[] wordsToFind,
                                       String[][] stringsToSearch) {
            mWordsToFind = wordsToFind;

            // Create an Iterator for the array of Strings to search.
            mInputIterator = Arrays.asList(stringsToSearch).iterator();
        }

        /**
         * Factory method that returns the next List of Strings to
         * be searched concurrently by the TaskGang.
         */
        @Override
        protected List<String> getNextInput() {
            if (mInputIterator.hasNext()) {
                // Note that we're starting a new cycle.
                incrementCycle();

                // Return a Vector containing the Strings to search
                // concurrently.
                return Arrays.asList(mInputIterator.next());
            }
            else 
                return null;
        }

        /**
         * Search for all instances of @code word in @code inputData
         * and return a list of all the @code SearchData results (if
         * any).
         */
        protected SearchResults searchForWord(String word, 
                                              String inputData) {
            SearchResults results =
                new SearchResults(Thread.currentThread().getId(),
                                  currentCycle(),
                                  word,
                                  inputData);

            // Check to see how many times (if any) the word appears
            // in the input data.
            for (int i = inputData.indexOf(word, 0);
                 i != -1;
                 i = inputData.indexOf(word, i + word.length())) {
                // Each time a match is found it's added to the list
                // of search results.
                results.add(new SearchResult(i));
            }
            return results;
        }

        /**
         * Hook method that can be used as an exit barrier to wait for
         * the gang of Threads to exit.
         */
        protected void awaitTasksDone() {
            getExecutorService().shutdown();
            try {
                // Wait for all the Threads in the pool to exit.
                getExecutorService().awaitTermination(Long.MAX_VALUE,
                                                      TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * @class SearchTaskGangExecutorOneShot
     *
     * @brief Customizes the SearchTaskGangCommon framework to process
     *        a one-shot List of tasks via a pool of Threads created
     *        by the ExecutorService, which is also used to wait for
     *        all the Threads in the pool to shutdown.
     */
    static public class SearchTaskGangExecutorOneShot
        extends SearchTaskGangCommon {
        /**
         * Controls when the framework exits.
         */
        protected CountDownLatch mExitBarrier = null;
        
        /**
         * Queue to store the results of concurrent computations.
         */
        private BlockingQueue<SearchResults> mResultsQueue = 
            new LinkedBlockingQueue<SearchResults>();

        /**
         * Number of Threads in the pool.
         */ 
        protected final int MAX_THREADS = 4;
        
        /**
         * Constructor initializes the superclass.
         */
        public SearchTaskGangExecutorOneShot(String[] wordsToFind,
                                      String[][] stringsToSearch) {
            // Pass input to superclass constructor.
            super(wordsToFind,
                  stringsToSearch);
        }

        /**
         * Hook method that initiates the gang of Threads by using a
         * fixed-size Thread pool managed by the ExecutorService.
         */
        @Override
            protected void initiateHook(int inputSize) {
            printDebugging("@@@ starting cycle "
                           + currentCycle()
                           + " with "
                           + inputSize
                           + " tasks@@@");
            // Initialize the exit barrier to inputSize, which causes
            // awaitTasksDone() to block until the cycle is finished.
            mExitBarrier = new CountDownLatch(inputSize);

            // Create a fixed-size Thread pool.
            if (getExecutorService() == null) 
                setExecutorService (Executors.newFixedThreadPool(MAX_THREADS));
        }

        /**
         * Initiate the TaskGang to run each worker in the Thread
         * pool.
         */
        protected void initiateTaskGang(int inputSize) {
            // Allow subclasses to customize their behavior before the
            // Threads in the gang are spawned.
            initiateHook(inputSize);

            // Enqueue each item in the input List for execution in the
            // Executor's Thread pool.
            for (int i = 0; i < inputSize; ++i) 
                getExecutorService().execute(makeTask(i));
        }

        /**
         * Runs in a background Thread and searches the inputData for
         * all occurrences of the words to find.
         */
        @Override
        protected boolean processInput (String inputData) {
            // Iterate through each word we're searching for
            // and try to find it in the inputData.
            for (String word : mWordsToFind) 
                // Each time a match is found the queueResults() hook
                // method is called to pass the search results to a
                // background Thread for concurrent processing.
                queueResults(searchForWord(word, 
                                           inputData));
            return true;
        }

        /**
         * Hook method called when a worker Thread is done - it
         * decrements the CountDownLatch.
         */
        @Override
        protected void taskDone(int index) throws IndexOutOfBoundsException {
            mExitBarrier.countDown();
        }

        /**
         * Hook method that can be used by processInput() to
         * process results.
         */
        protected void queueResults(SearchResults results) {
          getQueue().add(results);
        }

        /**
         * Hook method that shuts down the ExecutorService's Thread
         * pool and waits for all the Threads to exit before
         * returning.
         */
        @Override
        protected void awaitTasksDone() {
            do {
                // Start processing this cycle of results
                // concurrently.
                processQueuedResults(getInput().size() 
                                     * mWordsToFind.length);

                try {
                    // Wait until the exit barrier has been tripped.
                    mExitBarrier.await();
                } catch (InterruptedException e) {
                    printDebugging("await() interrupted");
                }

                // Keep looping as long as there's another cycle's
                // worth of input.
            } while (advanceTaskToNextCycle());

            // Call up to the super class to await for ExecutorService
            // Threads to shutdown.
            super.awaitTasksDone();
        }

        /**
         * Set the BlockingQueue, returning the existing queue.
         */
        protected BlockingQueue<SearchResults> setQueue(BlockingQueue<SearchResults> q) {
            BlockingQueue<SearchResults> old = mResultsQueue;
            mResultsQueue = q;
            return old;
        }

        /**
         * Get the BlockingQueue. 
         */
        protected BlockingQueue<SearchResults> getQueue() {
            return mResultsQueue;
        }

        /**
         * Process all the queued results concurrently.
         */
        protected void processQueuedResults(final int resultCount) {
            Runnable processQueuedResultsRunnable =
                new Runnable() {
                    public void run() {
                        try {
                            for (int i = 0; i < resultCount; ++i)
                                // Extract each SearchResults from the
                                // queue (blocking if necessary) until
                                // we're done.
                                getQueue().take().print();

                        } catch (InterruptedException e) {
                            printDebugging("run() interrupted");
                        }
                    }
                };

            // Create a new Thread that will process the results
            // concurrently.
            Thread t = new Thread(processQueuedResultsRunnable);
            t.start();

            try {
                // Wait for the results processing Thread to exit.
                t.join();
            } catch (InterruptedException e) {
                printDebugging("processQueuedResults() interrupted");
            }
        }
    }

    /**
     * @class SearchTaskGangExecutorCyclic
     *
     * @brief Customizes the SearchTaskGangCommon framework to process
     *        a cyclic List of tasks via a pool of Threads created by
     *        the ExecutorService, which is also used to wait for all
     *        the Threads in the pool to shutdown.
     */
    static public class SearchTaskGangExecutorCyclic
        extends SearchTaskGangExecutorOneShot {
        /**
         * Constructor initializes the superclass.
         */
        SearchTaskGangExecutorCyclic(String[] wordsToFind,
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
            // Use invokeAll() to execute all items in the collection
            // via the Executor's Thread pool.
                getExecutorService().invokeAll(workerCollection);
            } catch (InterruptedException e) {
                printDebugging("invokeAll() interrupted");
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

    /**
     * @class SearchTaskGangExecutorFutureOneShot
     * 
     * @brief ...
     */
    static public class SearchTaskGangExecutorFutureOneShot
        extends SearchTaskGangCommon {
        /**
         * A List of Futures that contain SearchResults.
         */
        protected List<Future<SearchResults>> mResultFutures;

        /**
         * Constructor initializes the data members.
         */
        protected SearchTaskGangExecutorFutureOneShot(String[] wordsToFind,
                                                      String[][] stringsToSearch) {
            // Pass input to superclass constructor.
            super(wordsToFind, 
                  stringsToSearch);

            // Initialize the ExecutorService with a cached pool of
            // Threads.
            setExecutorService (Executors.newCachedThreadPool());
        }

        /**
         * Process all the Futures containing search results.
         */
        protected void processFutureResults(List<Future<SearchResults>> resultFutures) {
            // Iterate through the List of Futures and print the
            // search results.  
            for (Future<SearchResults> resultFuture : resultFutures) {
                try {
                    // The get() call may block if the results aren't
                    // ready yet.
                    resultFuture.get().print();
                } catch (Exception e) {
                    printDebugging("get() exception");
                }
            }
        }

        /**
         * Hook method that performs work a background Task.  Returns true
         * if all goes well, else false (which will stop the background
         * Thread from continuing to run).
         */
        protected boolean processInput(final String inputData) {

            // Iterate through each word and submit a Callable that
            // will search concurrently for this word in the
            // inputData.
            for (final String word : mWordsToFind) {
                // Create a Future to store the results.
                final Future<SearchResults> resultFuture = 
                    getExecutorService().submit (new Callable<SearchResults>() {
                            @Override
                            public SearchResults call() throws Exception {
                                return searchForWord(word,
                                                     inputData);
                            }
                        });
                        
                // Add the Future to the List so it can be processed
                // later.
                mResultFutures.add(resultFuture);
            }
            return true;
        }

        /**
         * Initiate the TaskGang to process each word as a separate
         * task in the Thread pool.
         */
         protected void initiateTaskGang(int inputSize) {
             // Preallocate the List of Futures to hold all the
             // SearchResults.
             mResultFutures = 
                 new ArrayList<Future<SearchResults>> 
                     (inputSize * mWordsToFind.length);

             // Process each String of inputData via the
             // processInput() method.
             for (final String inputData : getInput())
                 processInput(inputData);

             // Process all the Futures.
             processFutureResults(mResultFutures);
         }
    }

    /**
     * @class SearchTaskGangExecutorCompletionOneShot
     *
     * @brief ...
     */
    static public class SearchTaskGangExecutorCompletionOneShot
        extends SearchTaskGangCommon {
        /**
         * Processes the results of Futures returned from the
         * ExecutorService.submit() method.
         */
        protected ExecutorCompletionService<SearchResults> mCompletionService;

        /**
         * Constructor initializes the data members.
         */
        protected SearchTaskGangExecutorCompletionOneShot(String[] wordsToFind,
                                                          String[][] stringsToSearch) {
            // Pass input to superclass constructor.
            super(wordsToFind, 
                  stringsToSearch);

            // Initialize the ExecutorService with a cached pool of
            // Threads.
            setExecutorService (Executors.newCachedThreadPool());

            // Connect the ExecutorService with the CompletionService
            // to process SearchResults concurrently. 
            mCompletionService =
                new ExecutorCompletionService<SearchResults>(getExecutorService());
        }

        /**
         * Uses the ExecutorCompletionService to concurrently process
         * all the queued Futures.
         */
        protected void concurrentlyProcessQueuedFutures() {
            // Need to account for all the input data and all the
            // words that were searched for.
            final int count = 
                getInput().size() * mWordsToFind.length;

            for (int i = 0; i < count; ++i) 
                try {
                    // Take the next ready Future off the
                    // CompletionService's queue and print the search
                    // results.
                    final Future<SearchResults> resultFuture =
                        mCompletionService.take();

                    // The get() call will not block since the results
                    // should be ready.
                    resultFuture.get().print();
                } catch (Exception e) {
                    printDebugging("get() exception");
                }
        }

        /**
         * Hook method that performs work a background Task.  Returns true
         * if all goes well, else false (which will stop the background
         * Thread from continuing to run).
         */
        protected boolean processInput(final String inputData) {

            // Iterate through each word and submit a Callable that
            // will search concurrently for this word in the
            // inputData.
            for (final String word : mWordsToFind) {
                // This submit() call stores the Future result in the
                // ExecutorCompletionService for concurrent
                // processing.
                mCompletionService.submit (new Callable<SearchResults>() {
                        @Override
                        public SearchResults call() throws Exception {
                            return searchForWord(word,
                                                 inputData);
                        }
                    });
            }
            return true;
        }

        /**
         * Initiate the TaskGang to process each word as a separate
         * task in the Thread pool.
         */
         protected void initiateTaskGang(int inputSize) {
             // Enqueue each item in the input List for execution in
             // the Executor's Thread pool.
             for (int i = 0; i < inputSize; ++i) 
                 getExecutorService().execute(makeTask(i));

             // Process all the Futures concurrently via the
             // ExecutorCompletionService
             concurrentlyProcessQueuedFutures();
         }
    }

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Factory method that creates the desired type of
     * SearchTaskGangCommon subclass implementation.
     */
    private static SearchTaskGangCommon makeTaskGang(String[] wordList,
                                                     TestsToRun choice) {
        switch(choice) {
        case EXECUTOR_ONESHOT:
            return new SearchTaskGangExecutorOneShot(wordList,
                                                     mOneShotInputStrings);
        case EXECUTOR_CYCLIC:
            return new SearchTaskGangExecutorCyclic(wordList,
                                                    mFixedNumberOfInputStrings);
        case EXECUTOR_FUTURE_ONESHOT:
            return new SearchTaskGangExecutorFutureOneShot(wordList,
                                                           mOneShotInputStrings);
        case EXECUTOR_COMPLETION_ONESHOT:
            return new SearchTaskGangExecutorCompletionOneShot(wordList,
                                                               mOneShotInputStrings);
        }
        return null;
    }
    
    /**
     * This is the entry point into the test program.  
     */
    public static void main(String[] args) {
        printDebugging("Starting TaskGangTest");
        
        // Create/run appropriate type of SearchTaskGang to search for
        // words concurrently.

        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting "
                           + test);
            makeTaskGang(mWordList, test).run();
            printDebugging("Ending "
                           + test);
        }

        printDebugging("Ending TaskGangTest");
    }
}
