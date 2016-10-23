import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Demonstrates the use of Java Thread.join() as a simple barrier
 * synchronizer to implement an "embarrassingly parallel" application
 * that concurrently searches for words in a List of Strings.
 */
public class ThreadJoinTest {
    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    private static boolean diagnosticsEnabled = true;

    /**
     * This input array is used by the ThreadJoinTest to search for
     * the words concurrently in multiple threads.
     */
    private final static String[] mOneShotInputStrings = 
    {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"};

    // List of words to search for.
    private final static String[] mWordList = {"do",
                                         "re",
                                         "mi",
                                         "fa",
                                         "so",
                                         "la",
                                         "ti",
                                         "do"};

    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) {
        System.out.println("Starting ThreadJoinTest");

        // Create/run an object to search for words concurrently.
        new SearchOneShotThreadJoin(mWordList, mOneShotInputStrings);

        System.out.println("Ending ThreadJoinTest");
    }

    /**
     * Starts a Thread for each element in the List of input Strings
     * and uses Thread.join() to wait for all the Threads to finish.
     * This implementation doesn't require any Java synchronization
     * mechanisms other than what's provided by Thread.
     */
    private static class SearchOneShotThreadJoin {
        /**
         * The array of words to find.
         */
        private final String[] mWordsToFind;

        /**
         * The List of worker Threads that were created.
         */
        private final List<Thread> mWorkerThreads;
        
        /**
         * Constructor initializes the data members.
         */
        public SearchOneShotThreadJoin(String[] wordsToFind,
                                       String[] inputStrings) {
            // Initialize field.
            mWordsToFind = wordsToFind;

            // Create a list that holds Threads so they can be joined when their processing is done.
            mWorkerThreads = makeWorkerThreads(this::processInput, Arrays.asList(inputStrings).iterator());

            // Start Thread to process its input in background.
            mWorkerThreads.forEach(Thread::start);

            // The forEach() method iterates through each thread in the list
            // and uses barrier synchronization to wait for threads to finish.
            mWorkerThreads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            /**
             * Can also use this solution:

             mWorkerThreads.forEach(thread -> {
                                    try {
                                    } catch (InterruptedException e) {
                                      throw new RuntimeException(e);
                                    }
             });

             */

        }

        /**
         * Create a list that holds Threads so they can be joined when their processing is done.
         * @param task
         * @param inputIterator
         * @return
         */
        ArrayList<Thread> makeWorkerThreads(Function<String, Void> task,
                                            Iterator<String> inputIterator) {
            ArrayList<Thread> arrayList = new ArrayList<>();

            while (inputIterator.hasNext()) {
                // Get next input data element.
                String element = inputIterator.next();

                // Create a Thread for each element in inputStrings to
                // perform processing designated by processInput().
                arrayList.add(new Thread(() // Create lambda to run in background Thread.
                              ->
                              // Apply the task to process the input data elements
                              task.apply(element)));
            }

            return arrayList;
        }

        /**
         * Run in a background thread and search the @a inputData for
         * all occurrences of the words to find.  Each time a match is
         * found the processResults() hook method is called to handle
         * the results.
         */
        private Void processInput(String inputData) {
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
            return null;
        }

        /**
         * Hook method that simple prints the results.
         */
        private void processResults(String results) {
            printDebugging(results);
        }

    }
  
    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled) 
            System.out.println(output);                
    }
}

