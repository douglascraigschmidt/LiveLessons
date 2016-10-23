import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

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
     * Starts a Thread for each element in the List of input Strings
     * and uses Thread.join() to wait for all the Threads to finish.
     * This implementation doesn't require any Java synchronization
     * mechanisms other than what's provided by Thread.
     */
    private static class SearchOneShotThreadJoin 
            implements Runnable {
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
            mWorkerThreads = makeWorkerThreads(this::processInput, Arrays.asList(inputStrings));
        }

        /**
         * Start the threads and run the test.
         */
        @Override
        public void run() {
            // Start Thread to process its input in background.
            mWorkerThreads.forEach(Thread::start);

            // The forEach() method iterates through each thread in the list
            // and uses barrier synchronization to wait for threads to finish.
            mWorkerThreads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));
        }

        /**
         * Create a list that holds Threads so they can be joined when their processing is done.
         * @param task
         * @param inputIterator
         * @return
         */
        List<Thread> makeWorkerThreads(Function<String, Void> task,
                                       List<String> inputList) {
            final Iterator<String> inputIterator = 
                inputList.iterator();

            // Create a list list that holds Threads so they can be
            // joined when their processing is done.
            return Stream
                // Create a Thread for each element in inputStrings to
                // perform processing designated by processInput().
                .generate(() 
                          // Create lambda to run in background Thread.
                          -> new Thread(() 
                                        ->
                                        // Apply the task to process
                                        // the input data elements
                                        task.apply(inputIterator.next())))
                .limit(inputList.size())

                // Return a list of Threads.
                .collect(toList());
        }

        /**
         * Run in a background thread and search the @a inputData for
         * all occurrences of the words to find.  Each time a match is
         * found the processResults() hook method is called to handle
         * the results.
         */
        private Void processInput(String inputData) {
            // Iterate through each word we're searching for.
            for (String word : mWordsToFind) {
                // This spliterator creates a stream of matches to a
                // word in the input data.
                WordMatcherSpliterator spliterator =
                    new WordMatcherSpliterator(new WordMatcher(word).with(inputData));

                // Use the spliterator to add the indices of all
                // places in the input data where word matches.
                StreamSupport
                    // Create an Integer parallelstream with indices
                    // indicating all the places (if any) where word
                    // matched the input data.
                    .stream(spliterator, true)

                    // Iterate through each index in the stream.
                    .forEach(index
                             -> {
                                 // Each time a match is found call
                                 // processResults() to handle
                                 // results.
                                 processResults("in thread "
                                                + Thread.currentThread().getId()
                                                + " "
                                                + word
                                                + " was found at offset "
                                                + index
                                                + " in string "
                                                + inputData);
                             });
            }
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

