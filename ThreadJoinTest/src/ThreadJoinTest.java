import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * @class ThreadJoinTest
 *
 * @brief This program tests various subclassses of the ThreadGang
 *        framework, which use different Java barrier synchronizers to
 *        implement an "embarraassingly parallel" application that
 *        concurrently searches for words in a Vector of Strings.
 */
public class ThreadJoinTest {
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
     * @brief Customizes the SearchThreadGangCommon framework to spawn
     *        a Thread for each element in the Vector of input Strings
     *        and uses Thread.join() to wait for all the Threads to
     *        finish.  This implementation doesn't require any Java
     *        synchronization mechanisms other than what's provided by
     *        Thread.
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
            mWordsToFind = wordsToFind;
            mInput = Arrays.asList(inputStrings);

            // This List holds Threads so they can be joined.
            mWorkerThreads = new LinkedList<Thread>();

            // Create and start a Thread for each element in the
            // mInput.  
            for (int i = 0; i < mInput.size(); ++i) {
                // Each Thread performs the processing designated by
                // the processInput() method of the worker Runnable.
                Thread t = new Thread(makeWorker(i));
                
                // Add to the List of Threads to join.
                mWorkerThreads.add(t);
                t.start();
            }        

            // Barrier synchronization.
            for (Thread thread : mWorkerThreads)
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
        }

        private Runnable makeWorker(final int index) {
            return new Runnable() {
                // This method runs in background Thread.
                public void run() {
                    try {
                        // Get the input data element associated with
                        // this index.
                        String element = mInput.get(index);

                        // Process input data element.
                        if (processInput(element) == false)
                            return;
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                }
            };
        }

        /**
         * Runs in a background Thread and searches the inputData for
         * all occurrences of the words to find.  Each time a match is
         * found the processResults() hook method is called to handle
         * the results.
         */
        private boolean processInput (String inputData) {
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

        /**
         * Hook method that processes the results.
         */
        private void processResults(String results) {
            // @@ NS: Need to do something more interesting here.
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
     
        // Create/run appropriate type of SearchThreadGang to search
        // for words concurrently.
        printDebugging("Starting JOIN");
        new SearchOneShotThreadJoin(mWordList,
                                    mOneShotInputStrings);
        printDebugging("Ending JOIN");

        printDebugging("Ending ThreadJoinTest");
    }
}

