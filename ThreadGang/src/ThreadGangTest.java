import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Phaser;

/**
 * @class ThreadGangTest
 *
 * @brief This program tests various subclassses of the ThreadGang
 *        framework, which use different Java barrier synchronizers to
 *        implement an "embarraassingly parallel" application that
 *        concurrently searches for words in a Vector of Strings.
 *
 * @@ NS: Need to improve the documentation.
 */
public class ThreadGangTest {
    // @@ NS: Need to use enums instead of ints.
    private final static int EXECUTOR = 0;
    private final static int JOIN = 1;
    private final static int COUNTDOWNLATCH = 2;
    private final static int CYCLIC = 3;
    private final static int PHASER = 4;

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true;

    // @@ NS: Need to get this data from files rather than from
    // hard-coded strings!

    /**
     * This input array is used by the one-shot tests that search for
     * the words concurrently in multiple threads.
     */
    private final static String[] mOneShotInputStrings[] = { 
        {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a fixed number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[] mFixedNumberOfInputStrings[] = { 
        {"xdoodoo", "xreo", "xmiomio", "xfao", "xsoosoo", "xlao", "xtiotio", "xdoo"},
        {"xdoo", "xreoreo", "xmio", "xfaofao", "xsoo", "xlaolao", "xtio", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a variable number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[] mVariableNumberOfInputStrings[] = {
        {"xfaofao"},
        {"xsoo", "xlaolao", "xtio", "xdoodoo"},
        {"xdoo", "xreoreo"},
        {"xdoodoo", "xreo", "xmiomio"}
    };

    /**
     * @class SearchThreadGangCommon
     * 
     * @brief This helper class factors out the common code used by
     *        all the implementations of ThreadGang below.  It
     *        customizes the ThreadGang framework to concurrently
     *        search an array of Strings for an array of words to
     *        find.
     */
    static public abstract class SearchThreadGangCommon
                  extends ThreadGang<String, String> {
        /**
         * The array of words to find.
         */
        final String[] mWordsToFind;
        
        /**
         * Number of arrays of strings to search.
         */
        protected int mCount;
        
        /**
         * Factory method that returns the next Vector of Strings to
         * be searched concurrently by the one-shot Threads gangs.
         */
        @Override
        protected Vector<String> getNextInput() {
            if (mCount-- > 0) 
                return new Vector<String>(Arrays.asList(mOneShotInputStrings[mCount]));
            else 
                return null;
        }

        /**
         * Constructor initializes the data member.
         */
        public SearchThreadGangCommon(String[] wordsToFind) {
            mWordsToFind = wordsToFind;
            mCount = mOneShotInputStrings.length;
        }

        /**
         * Hook method that processes the results.
         */
        protected void processResults(String results) {
            // @@ NS: Need to do something more interesting here.
            printDebugging(results);
        }

        /**
         * Runs in a background Thread and searches the inputData for
         * all occurrences of the words to find.  Each time a match is
         * found the processResults() hook method is called to handle
         * the results.
         */
        public boolean doWorkInBackground (String inputData) {
            // Iterate through each word we're searching for.
            for (String word : mWordsToFind) 
                // Check to see how many times (if any) the word
                // appears in the input data.
                for (int i = inputData.indexOf(word, 0);
                     i != -1;
                     i = inputData.indexOf(word, i + word.length()))
                    // Each time a match is found the processResults()
                    // hook method is called to handle the results.
                    processResults("in thread " 
                                   + Thread.currentThread().getId()
                                   + " "
                                   + word
                                   + " was found at offset "
                                   + i
                                   + " in string "
                                   + inputData);
            return true;
        }
    }

    /**
     * @class SearchOneShotThreadGangJoin
     *
     * @brief Customizes the SearchThreadGangCommon framework to spawn
     *        a Thread for each element in the Vector of input Strings
     *        and uses Thread.join() to wait for all the Threads to
     *        finish.  This implementation doesn't require any Java
     *        synchronization mechanisms other than what's provided by
     *        Thread.
     */
    static public class SearchOneShotThreadGangJoin 
                  extends SearchThreadGangCommon {
        /**
         * The List of worker Threads that were created.
         */
        private List<Thread> mWorkerThreads;
        
        /**
         * Constructor initializes the super class.
         */
        SearchOneShotThreadGangJoin(String[] wordsToFind) {
            // Pass input to search to superclass constructor.
            super(wordsToFind);
        }

        /**
         * Hook method that initiates the gang of Threads.
         */
        protected void initiateThreadGang(int size) {
            // This List holds the Threads so they can be joined in
            // awaitThreadGangDone().
            mWorkerThreads = new LinkedList<Thread>();

            // Create and start a Thread for each element in the input
            // Vector - each Thread performs the processing designated
            // by the doWorkInBackgroundThread() hook method.
            for (int i = 0; i < size; ++i) {
                Thread t = new Thread(makeWorker(i));
                mWorkerThreads.add(t);
                t.start();
            }
        }

        /**
         * Hook method that uses calls to Thread.join() as an exit
         * barrier to wait for the gang of Threads to exit.
         */
        protected void awaitThreadGangDone() {
            try {
                for (Thread thread : mWorkerThreads)
                    thread.join();
            } catch (InterruptedException e) {
            }
        }
    }
  
    /**
     * @class SearchOnThreadGangCountDownLatch
     *
     * @brief Customizes the SearchThreadGangCommon framework to spawn
     *        a Thread for each element in the Vector of input Strings
     *        and uses CountDownLatch to wait for all the Threads to
     *        finish.
     */
    static public class SearchOneShotThreadGangCountDownLatch 
                  extends SearchThreadGangCommon {
        /**
         * CountDownLatch that's used to coordinate the processing
         * threads.
         */
        protected CountDownLatch mBarrier;
        
        /**
         * Constructor initializes the super class.
         */
        SearchOneShotThreadGangCountDownLatch(String[] wordsToFind) {
            // Pass input to search to superclass constructor.
            super(wordsToFind);
        }

        /**
         * Hook method that initiates the gang of Threads.
         */
        protected void initiateThreadGang(int size) {
            // Create a CountDownLatch whose count corresponds to each
            // element in the input Vector.
            mBarrier = new CountDownLatch(size);

            // Create and start a Thread for each element in the input
            // Vector - each Thread performs the processing designated
            // by the doWorkInBackgroundThread() hook method.
            for (int i = 0; i < size; ++i) 
                new Thread(makeWorker(i)).start();
        }

        /**
         * Hook method called when a worker Thread is done - it
         * decrements the CountDownLatch.
         */
        protected void workerDone(int index) throws IndexOutOfBoundsException {
            mBarrier.countDown();
        }
        
        /**
         * Hook method that uses the CountDownLatch as an exit barrier
         * to wait for the gang of Threads to exit.
         */
        protected void awaitThreadGangDone() {
            try {
                mBarrier.await();
            } catch (InterruptedException e) {
            }
        }
    }
  
    /**
     * @class SearchOneShotThreadGangExecutor
     *
     * @brief Customizes the SearchThreadGangCommon framework to spawn
     *        a pool of Threads via the ExecutorService, which is also
     *        used to wait for all the Threads to shutdown.
     */
    static public class SearchOneShotThreadGangExecutor
                  extends SearchThreadGangCommon {
        /**
         * Executes submitted Runnable tasks in a Thread pool.
         */
        private ExecutorService mExecutorService;
        
        /**
         * Number of Threads in the pool.
         */ 
        private final int MAX_THREADS = 4;
        
        /**
         * Constructor initializes the superclass.
         */
        SearchOneShotThreadGangExecutor(String[] wordsToFind) {
            // Pass input to search to superclass constructor.
            super(wordsToFind);
        }

        /**
         * Hook method that initiates the gang of Threads by using a
         * fixed-size Thread pool managed by the ExecutorService.
         */
        protected void initiateThreadGang(int size) {
            // Create a fixed-size Thread pool.
            mExecutorService =
                Executors.newFixedThreadPool(MAX_THREADS);

            // Enqueue each item in the input Vector for execution in
            // the Executor's Thread pool.
            for (int i = 0; i < size; ++i) 
                mExecutorService.execute(makeWorker(i));
        }

        /**
         * Hook method that shuts down the ExecutorService's Thread
         * pool and waits for all the Threads to exit before
         * returning.
         */
        protected void awaitThreadGangDone() {
            // This call waits until the existing queue of Runnables
            // is processed by the Thread pool before shutting down.
            mExecutorService.shutdown();

            try {
                // Wait for all the Threads in the pool to exit.
                mExecutorService.awaitTermination(Long.MAX_VALUE,
                                                  TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }
        }
    }
  
    /**
     * @class SearchCyclicThreadGang
     *
     * @brief Customizes the SearchThreadGangCommon framework with a
     *        CyclicBarrier to continue searching a fixed number of
     *        input Strings via a fixed number of Threads until
     *        there's no more input to process.
     */
    static public class SearchCyclicThreadGang 
                  extends SearchThreadGangCommon {
        /**
         * The barrier that's used to coordinate each cycle, i.e.,
         * each Thread must await on mBarrier for all the other
         * Threads to complete their processing before they all
         * attempt to move to the next cycle en masse.
         */
        protected CyclicBarrier mBarrier;

        /**
         * Controls when the framework exits.
         */
        final CountDownLatch mExitLatch;
        
        /**
         * Constructor initializes the data members and superclass.
         */
        SearchCyclicThreadGang(String[] wordsToFind) {
            // Pass input to search to superclass constructor.
            super(wordsToFind);

            mCount = mFixedNumberOfInputStrings.length;

            // Initialize the exit latch to 1, which causes
            // awaitThreadGangDone() to block until the test is
            // finished.
            mExitLatch = new CountDownLatch(1);
        }

        /**
         * Factory method that returns the next Vector of Strings to
         * be searched concurrently by the gang of Threads.
         */
        @Override
        protected Vector<String> getNextInput() {
            if (mCount-- > 0) 
                return new Vector<String>(Arrays.asList
                                          (mFixedNumberOfInputStrings[mCount]));
            else 
                return null;
        }

        /**
         * Each Thread in the gang uses a call to CyclicBarrier
         * await() to wait for all the other Threads to complete their
         * current cycle.
         */
        protected void workerDone(int index) throws IndexOutOfBoundsException {
            try {
                mBarrier.await();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } 
        }

        /**
         * Hook method that initiates the gang of Threads.
         */
        protected void initiateThreadGang(int size) {
            // Create a CyclicBarrier whose (1) "parties" count
            // corresponds to each element in the input Vector and (2)
            // barrier action gets the next Vector of input data (if
            // any).
            mBarrier = new CyclicBarrier
                (size,
                 new Runnable() {
                     public void run() {
                         setVector(getNextInput());
                         if (getVector() != null)
                             printDebugging("@@@@@ Started next cycle @@@@@");
                     }
                 });

            // Create and start a Thread for each element in the input
            // Vector - each Thread performs the processing designated
            // by the doWorkInBackgroundThread() hook method.
            for (int i = 0; i < size; ++i)
                new Thread(makeWorker(i)).start();
        }

        /**
         * When there's no more input data to process release the exit
         * latch and return false so the worker Thread will return.
         * Otherwise, return true so the worker Thread will continue
         * to run.
         */
        @Override
        protected boolean advanceToNextCycle() {
            if (getVector() == null) {
                mExitLatch.countDown();
                return false;
            } else
            	return true;
        }

        /**
         * Waits on an exit latch for the gang of Threads to exit.
         */
        @Override
        protected void awaitThreadGangDone() {
            try {
                mExitLatch.await();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * @class SearchPhaserThreadGang
     *
     * @brief Customizes the SearchThreadGangCommon framework to to
     *        continue to search a variable number of words/Threads
     *        concurrently until there's no more input to process.
     */
    static public class SearchPhaserThreadGang 
                  extends SearchThreadGangCommon {
        /**
         * The barrier that's used to coordinate each cycle, i.e.,
         * each Thread must await on mBarrier for all the other
         * Threads to complete their processing before they all
         * attempt to move to the next cycle en masse.
         */
        protected Phaser mPhaser;

        /**
         * Indicate that the size of the input Vector has changed,
         * which requires a reconfiguration to add or remove Threads
         * from the gang.
         */
        volatile int mReconfiguration;
        
        /**
         * Synchronizes all the Threads during the reconfiguration.
         */
        volatile CyclicBarrier mReconfigurationBarrier;

        /**
         * Controls when the framework exits.
         */
        final CountDownLatch mExitLatch;

        /**
         * Constructor initializes the data members and superclass.
         */
        SearchPhaserThreadGang(String[] wordsToFind) {
            // Pass input to search to superclass constructor.
            super(wordsToFind);
            mReconfiguration = 0;
            mCount = mVariableNumberOfInputStrings.length;

            // Initialize the exit latch to 1, which causes
            // awaitThreadGangDone() to block until the test is
            // finished.
            mExitLatch = new CountDownLatch(1);

            // Create a Phaser that controls how the Threads
            // synchronize on a dynamically reconfigurable barrier.
            mPhaser = new Phaser() {
                // Perform the actions upon impending phase advance.
                protected boolean onAdvance(int phase,
                                            int registeredParties) {
                    // Record the old input size to see if we need to
                    // reconfigure or not.
                    int oldSize = getVector().size();

                    // Get the new input.
                    setVector(getNextInput());

                    // Bail out if there's no input or no registered
                    // parties.
                    if (getVector() == null || registeredParties == 0)
                        return true;
                    else {
                        int newSize = getVector().size();

                        // See if we need to reconfigure the Phase due
                        // to changes in the size of the input Vector.
                        mReconfiguration = newSize - oldSize;

                        if (mReconfiguration == 0) 
                            printDebugging("@@@@@ Started next cycle with same Thread # ("
                                           + newSize
                                           + ") @@@@@");
                        else {
                            printDebugging("@@@@@ Started next cycle with "
                                           + newSize
                                           + " Threads compared with "
                                           + oldSize
                                           + " Threads @@@@@");

                            // Create a new CyclicBarrier to manage
                            // the reconfiguration.
                            mReconfigurationBarrier = new CyclicBarrier
                                (oldSize,
                                 // Create the barrier action.
                                 new Runnable() {
                                     public void run() {
                                         // If there are more elements
                                         // in the input Vector than
                                         // last time create/run new
                                         // worker Threads to process
                                         // them.
                                         if (oldSize < newSize)
                                             for (int i = oldSize; i < newSize; ++i)
                                                 new Thread(makeWorker(i)).start();

                                         // Indicate there's no more need
                                         // for reconfiguration.
                                         mReconfiguration = 0;
                                     }
                                 });
                        }
                        return false;
                    }
                }
            };
        }

        /**
         * Factory method that returns the next Vector of Strings to
         * be searched concurrently by the gang of Threads.
         */
        @Override
        protected Vector<String> getNextInput() {
            if (mCount-- > 0) 
                return new Vector<String>(Arrays.asList(mVariableNumberOfInputStrings[mCount]));
            else 
                return null;
        }

        /**
         * Hook method that initiates the gang of Threads.
         */
        protected void initiateThreadGang(int size) {
            // Create and start a Thread for each element in the input
            // Vector - each Thread performs the processing designated
            // by the doWorkInBackgroundThread() hook method.
            for (int i = 0; i < size; ++i)
                new Thread(makeWorker(i)).start();
        }

        /**
         * Each Thread in the gang uses a call to the Phaser
         * arriveAndAwaitAdvance() method to wait for all the other
         * Threads to complete their current cycle.
         */
        protected void workerDone(int index) throws IndexOutOfBoundsException {
            boolean throwException = false;
            try {
                // Wait until all other Threads are done with their
                // cycle.
                mPhaser.arriveAndAwaitAdvance();

                // Check to see a reconfiguration is needed.
                if (mReconfiguration != 0) {
                    try {
                        // Wait for all the existing threads to reach
                        // this barrier.
                        mReconfigurationBarrier.await();

                        // Check to see if this worker is no longer
                        // needed, i.e., due to the input Vector
                        // shrinking relative to the previous input
                        // Vector.
                        if (index >= getVector().size()) {
                            // Remove ourselves from the count of
                            // parties that will wait on this Phaser.
                            mPhaser.arriveAndDeregister();

                            // Indicate that we need to throw the
                            // IndexoutOfBoundsException so this
                            // Thread will be stopped.
                            throwException = true;
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    } 
                }                    
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } 

            // Throw this exception, which triggers the worker Thread
            // to exit.
            if (throwException)
                throw new IndexOutOfBoundsException();                
        }

        /**
         * Factory method that creates a Runnable worker that will
         * process one node of the input Vector (at location @code
         * index) in a background Thread.
         */
        protected Runnable makeWorker(final int index) {
            // Register ourselves with the Phaser so we're included in
            // it's registered parties.
            mPhaser.register();
            
            // Forward the rest of the processing to the superclass's
            // makeWorker() factory method.
            return super.makeWorker(index);
        }

        /**
         * When there's no more input data to process release the exit
         * latch and return false so the worker Thread will return.
         * Otherwise, return true so the worker Thread will continue
         * to run.
         */
        @Override
        protected boolean advanceToNextCycle() {
            if (getVector() == null) { // mPhaser.isTerminated()
                mExitLatch.countDown();
                return false;
            } else
            	return true;
        }

        /**
         * Waits on an exit latch for the gang of Threads to exit.
         */
        protected void awaitThreadGangDone() {
            try {
                mExitLatch.await();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Factory method that creates the desired type of
     * SearchThreadGangCommon subclass implementation.
     */
    private static SearchThreadGangCommon 
                   makeThreadGang(String[] wordList,
                                  int choice) {
    	SearchThreadGangCommon s = null;
        // @@ NS: need to replace with enums.
        switch(choice) {
        case EXECUTOR:
            s = new SearchOneShotThreadGangExecutor(wordList);
            break;
        case JOIN:
            s = new SearchOneShotThreadGangJoin(wordList);
            break;
        case COUNTDOWNLATCH:
            s = new SearchOneShotThreadGangCountDownLatch(wordList);
            break;
        case CYCLIC:
            s = new SearchCyclicThreadGang(wordList);
            break;
        case PHASER:
            s = new SearchPhaserThreadGang(wordList);
            break;
        }
        return s;
    }
    
    /**
     * This is the entry point into the test program.  
     */
    static public void main(String[] args) {
    	printDebugging("Starting ThreadGangTest");
     
        // List of words to search for.
        String[] wordList = {"do",
                             "re",
                             "mi",
                             "fa",
                             "so",
                             "la",
                             "ti",
                             "do"};
        
        // @@ NS: Need to improve so that the input comes from file,
        // not from hard-coded strings!

        // @@ NS: Need to improve so that it iterates through all the
        // enums rather than being hard-coded.  Also, need to add
        // timing statistics.

        // Create/run appropriate type of SearchThreadGang to search
        // for words concurrently.
        printDebugging("Starting COUNTDOWNLATCH");
        makeThreadGang(wordList, COUNTDOWNLATCH).run();
        printDebugging("Ending COUNTDOWNLATCH");

        printDebugging("Starting EXECUTOR");
        makeThreadGang(wordList, EXECUTOR).run();
        printDebugging("Ending EXECUTOR");
        
        printDebugging("Starting JOIN");
        makeThreadGang(wordList, JOIN).run();
        printDebugging("Ending JOIN");

        printDebugging("Starting CYCLIC");
        makeThreadGang(wordList, CYCLIC).run();
        printDebugging("Ending CYCLIC");

        printDebugging("Starting PHASER");
        makeThreadGang(wordList, PHASER).run();
        printDebugging("Ending PHASER");

        printDebugging("Ending ThreadGangTest");
    }
}

