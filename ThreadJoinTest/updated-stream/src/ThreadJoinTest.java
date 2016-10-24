import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This example implements an "embarrassingly parallel" application
 * that concurrently searches for words in a List of Strings.  It
 * demonstrates the use of Java 8 functional programming features
 * (such as lambda expressions, method references, functional
 * interfaces, and streams) in conjunction with Thread.start() to run
 * threads and Thread.join() to wait for all threads to finish
 * running.
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
     * mechanisms other than what's provided by the Thread class.
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
            return inputList
                // Convert the list into a stream.
                .stream()

                // Create a thread for each input stream element to
                // perform the processing designed by processInput().
                .map(element -> new Thread(() -> task.apply(element)))
 
                // Return a list of threads.
                .collect(toList());

            /*
            // Here's an alternative solution using Stream.generate():

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
                */
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

