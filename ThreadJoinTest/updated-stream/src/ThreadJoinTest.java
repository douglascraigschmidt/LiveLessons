import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This example implements an "embarrassingly parallel" application
 * that concurrently searches for words in a List of Strings.  It
 * demonstrates the use of Java 8 functional programming features
 * (such as lambda expressions, method references, functional
 * interfaces, streams, and spliterators) in conjunction with
 * Thread.start() to run a thread and Thread.join() to wait for the
 * thread to finish running.
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
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
        "do", "re", "mi", "fa", "so", "la", "ti", "do",
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
        private final List<Runnable> mWorkerRunnables;
        
        /**
         * The constructor initializes the fields.
         */
        public SearchOneShotThreadJoin(String[] wordsToFind,
                                       String[] inputStrings) {
            // Initialize field.
            mWordsToFind = wordsToFind;

            // Call the makeWorkerRunnables() factory method to create
            // a list of Runnables that will run the processInput()
            // method reference.
            mWorkerRunnables =
                makeWorkerRunnables(this::processInput,
                                    Arrays.asList(inputStrings));
        }

        /**
         * Start a thread to perform the concurrent searches in the
         * background and use Thread.join() to wait for the thread to
         * finish.  The use of an extra thread here is overkill - it's
         * just there to show how to use Thread.join() and to show
         * how to create a thread using a lambda expression.
         */
        @Override
        public void run() {
            // Create a new thread.
            Thread thread =
                new Thread(() ->
                           // Create a lambda expression that iterates
                           // through the runnables list and passes a
                           // method reference that runs each runnable
                           // on each input string.
                           mWorkerRunnables.forEach(Runnable::run));

            // Start the new thread.
            thread.start();

            try {
                // Join with the new thread.
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        /**
         * This factory method creates a list of Runnables.
         *
         * @param task Function to run in each thread.
         * @param inputList List of strings to search.
         * @return List of threads that will run the @a task.
         */
        List<Runnable> makeWorkerRunnables(Function<String, Void> task,
                                           List<String> inputList) {
            return inputList
                // Convert the list into a stream.
                .stream()

                // Create a lambda expression runnable for each input
                // stream element that performs the processing
                // designated by processInput().
                .map(element -> (Runnable) () -> task.apply(element))
 
                // Return a list of runnables.
                .collect(toList());

            /*
            // Here's an alternative solution using Stream.generate():

            final Iterator<String> inputIterator =
                inputList.iterator();

            // Create a list list that holds runnables.
            return Stream
                // Create a Runnable for each element in inputStrings to
                // perform processing designated by processInput().
                .generate(() 
                          // Create lambda to run in background Thread.
                          -> new Runnable() {
                                 public void run() {
                                        // Apply the task to process
                                        // the input data elements
                                        task.apply(inputIterator.next());
                                }
                             })
                .limit(inputList.size())

                // Return a list of runnables.
                .collect(toList());
                */
        }

        /**
         * This method runs in a background thread and searches the @a
         * inputData for all occurrences of the words to find.
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

