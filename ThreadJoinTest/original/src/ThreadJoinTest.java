import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * @class ThreadJoinTest
 *
 * @brief Demonstrates the use of Java Thread.join() as a simple
 *        barrier synchronizer to implement an "embarrassingly
 *        parallel" application that concurrently searches for words
 *        in a List of Strings.
 */
public class ThreadJoinTest {
    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true;

    /**
     * This input array is used by the ThreadJoinTest to search for
     * the words concurrently in multiple threads.
     */
    private final static String[] mOneShotInputStrings = 
    {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"};

    // List of words to search for.
    private static String[] mWordList = {"do",
                                         "re",
                                         "mi",
                                         "fa",
                                         "so",
                                         "la",
                                         "ti",
                                         "do"};
        
    /**
     * @class SearchOneShotThreadGangJoin
     *
     * @brief Starts a Thread for each element in the List of input
     *        Strings and uses Thread.join() to wait for all the
     *        Threads to finish.  This implementation doesn't require
     *        any Java synchronization mechanisms other than what's
     *        provided by Thread.
     */
    static public class SearchOneShotThreadJoin {
        /**
         * The input List that's processed.
         */
        private volatile List<String> mInput = null;

        /**
         * The array of words to find.
         */
        final String[] mWordsToFind;
        
        /**
         * The List of worker Threads that were created.
         */
        private List<Thread> mWorkerThreads;
        
        /**
         * Constructor initializes the data members.
         */
        public SearchOneShotThreadJoin(String[] wordsToFind,
                                       String[] inputStrings) {
            // Initialize the data members.
            mWordsToFind = wordsToFind;
            mInput = Arrays.asList(inputStrings);

            // This List holds Threads so they can be joined when their processing is done.
            mWorkerThreads = new LinkedList<>();

            // Create and start a Thread for each element in the
            // mInput.  
            for (int i = 0; i < mInput.size(); ++i) {
                // Each Thread performs the processing designated by
                // the processInput() method of the worker Runnable.
                Thread t = new Thread(makeTask(i));
                
                // Add to the List of Threads to join.
                mWorkerThreads.add(t);               
            }        

            // Start all threads to process input in the background.
            for (Thread thread : mWorkerThreads)
                thread.start();

            // Barrier synchronization to wait for threads to finish.
            for (Thread thread : mWorkerThreads)
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    printDebugging("join() interrupted");
                }
        }

        /**
         * Factory method that creates a Runnable task that will
         * process one node of the input List (at location @code
         * index) in a background Thread .
         */
        private Runnable makeTask(final int index) {
            return new Runnable() {
                // This method runs in background Thread.
                public void run() {
                    // Get the input data element associated with
                    // this index.
                    String element = mInput.get(index);
                    
                    // Process input data element.
                    processInput(element);
                }
            };
        }

        /**
         * Run in a background Thread and search the inputData for
         * all occurrences of the words to find.  Each time a match is
         * found the processResults() hook method is called to handle
         * the results.
         */
        private void processInput (String inputData) {
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
        }

        /**
         * Hook method that processes the results.
         */
        private void processResults(String results) {
            printDebugging(results);
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
     * This is the entry point into the test program.  
     */
    static public void main(String[] args) {
    	printDebugging("Starting ThreadJoinTest");
     
        // Create/run an object to search for words concurrently.
        new SearchOneShotThreadJoin(mWordList,
                                    mOneShotInputStrings);

        printDebugging("Ending ThreadJoinTest");
    }
}

