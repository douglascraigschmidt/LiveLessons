import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This example implements an "embarrassingly parallel" application
 * that concurrently searches for words in a List of Strings.  It
 * demonstrates the use of Java 8 functional programming features
 * (such as lambda expressions, method references, and functional
 * interfaces) in conjunction with Thread.start() to run threads and
 * Thread.join() to wait for all threads to finish running.
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

    /**
     * List of words to search for.
     */
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
     * mechanisms other than what's provided by the Thread class.
     */
    private static class SearchOneShotThreadJoin 
            implements Runnable {
        /**
         * The array of words to find.
         */
        private final String[] mWordsToFind;

        /**
         * The list of worker threads that were created.
         */
        private final List<Thread> mWorkerThreads;
        
        /**
         * The constructor initializes the fields.
         */
        public SearchOneShotThreadJoin(String[] wordsToFind,
                                       String[] inputStrings) {
            // Initialize field.
            mWordsToFind = wordsToFind;

            // Call the makeWorkerThreads() factory method to create a
            // list of threads that will be joined after they process
            // the input strings.  Each thread runs the processInput()
            // method reference passed to makeWorkerThreads().
            mWorkerThreads =
                makeWorkerThreads(this::processInput,
                                  Arrays.asList(inputStrings));
        }

        /**
         * Start the threads to perform the concurrent searches.
         */
        @Override
        public void run() {
            // Iterate through the list of threads & pass a method
            // reference that starts a thread for each input string.
            mWorkerThreads.forEach(Thread::start);

            // Iterate through the threads and pass the Thread.join()
            // method reference as a barrier synchronizer to wait for
            // each thread to finish.  Note how rethrowConsumer()
            // converts a checked exception to an unchecked exception.
            mWorkerThreads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            /*
              // This more verbose solution based on a lambda
              // expression can be used instead of rethrowConsumer():

              mWorkerThreads.forEach(thread -> {
                                     try {
                                         thread.join();
                                     } catch (InterruptedException e) {
                                       throw new RuntimeException(e);
                                     }});
            */
        }

        /**
         * This factory method creates a list of threads that can be
         * joined when their processing is done.
         *
         * @param task Function to run in each thread.
         * @param inputList List of strings to search.
         * @return List of threads that will run the @a task.
         */
        List<Thread> makeWorkerThreads(Function<String, Void> task,
                                       List<String> inputList) {
            // Create a new list.
            List<Thread> list = new ArrayList<>();

            // Create a thread for each input string to perform
            // processing designated by the task parameter.
            for (String element : inputList)
                list.add(new Thread(()
                         // Create lambda to run in thread.
                         ->
                         // Apply the task to process the
                         // input string.
                         task.apply(element)));

            return list;
        }

        /**
         * This method runs in a background thread and searches the @a
         * inputData for all occurrences of the words to find.
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

