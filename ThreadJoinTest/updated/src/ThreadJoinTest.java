import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Demonstrates the use of Java 8 functional programming features
 * (such as lambda expressions, method references, and functional
 * interfaces) and Thread.join() as a simple barrier synchronizer to
 * implement an "embarrassingly parallel" application that
 * concurrently searches for words in a List of Strings.
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
    private final static String[] mOneShotInputStrings = {
        "xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"
    };

    // List of words to search for.
    private final static String[] mWordList = {
        "do", "re", "mi", "fa", "so", "la", "ti", "do"
    };

    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) {
        System.out.println("Starting ThreadJoinTest");

        // Create/run an object to search for words concurrently.
        new SearchOneShotThreadJoin(mWordList, mOneShotInputStrings).run();

        System.out.println("Ending ThreadJoinTest");
    }

    /**
     * This class starts a thread for each element in the list of
     * input strings and uses Thread.join() to wait for all threads to
     * finish.  This implementation requires no Java synchronization
     * mechanisms other than what's provided by thread.
     */
    private static class SearchOneShotThreadJoin 
            implements Runnable {
        /**
         * The array of words to find.
         */
        private final String[] mWordsToFind;

        /**
         * The List of worker threads that were created.
         */
        private final List<Thread> mWorkerThreads;
        
        /**
         * Constructor initializes the data members.
         */
        public SearchOneShotThreadJoin(String[] wordsToFind,
                                       String[] inputStrings) {
            // Initialize field.
            mWordsToFind = wordsToFind;

            // Call a factory method to create a list that holds
            // threads to be joined when their processing is done.
            // Each thread will run the processInput() method.
            mWorkerThreads =
                makeWorkerThreads(this::processInput,
                                  Arrays.asList(inputStrings));
        }

        /**
         * Start the threads and run the test.
         */
        @Override
        public void run() {
            // Start a thread to process its input in background.
            mWorkerThreads.forEach(Thread::start);

            // Iterate through each thread in the list and use barrier
            // synchronization to wait for threads to finish.
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
         * Create a list that holds threads so they can be joined when their processing is done.
         *
         * @param task Function to run in each thread.
         * @param inputList List of strings to search.
         * @return List of threads that will run the @a task.
         */
        List<Thread> makeWorkerThreads(Function<String, Void> task,
                                       List<String> inputList) {
            // Create a new list.
            List<Thread> list = new ArrayList<>();

            // Iterate through all the strings in the inputList.
            for (Iterator<String> inputIterator = inputList.iterator();
                 inputIterator.hasNext();
                 ) {
                // Get next input data element.
                String element = inputIterator.next();

                // Create a thread for each input string to perform
                // processing designated by the task parameter.
                list.add(new Thread(() 
                                    // Create lambda to run in thread.
                                    ->
                                    // Apply the task to process the
                                    // input string.
                                    task.apply(element)));
            }

            return list;
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

