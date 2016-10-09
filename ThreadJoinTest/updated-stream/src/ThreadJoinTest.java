import java.util.Arrays;
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
     * Starts a Thread for each element in the List of input Strings
     * and uses Thread.join() to wait for all the Threads to finish.
     * This implementation doesn't require any Java synchronization
     * mechanisms other than what's provided by Thread.
     */
    static public class SearchOneShotThreadJoin {
        /**
         * The index for the next task.
         */
        int mIndex;
        
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
            // Initialize field.
            mWordsToFind = wordsToFind;

            // Create a function that performs the word counting task.
            Function<String, Void> task = this::processInput;

            // Create an InputProcessingThread object used to create
            // and run a Thread that searches for all words in each
            // input string.
            InputProcessingThread<String> threadFactory =
                new InputProcessingThread(task,
                                          Arrays.asList(inputStrings));

            // Create a list list that holds Threads so they can be
            // joined when their processing is done.
            mWorkerThreads = Stream
                // Create a Thread for each element in inputStrings to
                // perform processing designated by processInput().
                .generate(threadFactory::createThread)
                .limit(threadFactory.size())

                // Return a list of Threads.
                .collect(toList());

            // Start Thread to process its input in background.
            mWorkerThreads.forEach(Thread::start);

            // Barrier synchronization to wait for threads to finish.
            mWorkerThreads.forEach(thread
                                   -> { try {
                                           thread.join();
                                       } catch (InterruptedException e) {
                                           System.out.println("join() interrupted");
                                       }});
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
    	System.out.println("Starting ThreadJoinTest");
     
        // Create/run an object to search for words concurrently.
        new SearchOneShotThreadJoin(mWordList,
                                    mOneShotInputStrings);

        System.out.println("Ending ThreadJoinTest");
    }
}

