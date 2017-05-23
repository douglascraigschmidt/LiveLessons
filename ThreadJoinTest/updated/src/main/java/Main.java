import utils.ExceptionUtils;
import utils.TestDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This program implements an "embarrassingly parallel" application
 * that concurrently searches for phrases in a list of strings
 * containing the complete works of William Shakespeare.  It
 * demonstrates the use of Java 8 functional programming features
 * (such as lambda expressions, method references, and functional
 * interfaces) in conjunction with Thread.start() to run threads and
 * Thread.join() to wait for all running threads.
 */
public class Main {
    /*
     * Input files.
     */

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A list of phrases to search for in the complete works of
     * Shakespeare.
     */
    private static final String sPHRASE_LIST_FILE =
        "phraseList.txt";

    /**
     * The list of strings containing the complete works of
     * Shakespeare.
     */
    private static List<String> mInputList;

    /**
     * The list of phrases to find.
     */
    private static List<String> mPhrasesToFind;
        
    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Create a list of input strings containing the works of
        // Shakespeare.
        mInputList = TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE, "@");

        // This list will hold the list of phrases to find.
        mPhrasesToFind = new ArrayList<>();

        // Get the list of phrases to find in the works of Shakespeare.
        //noinspection ConstantConditions
        for (String phrase : TestDataFactory.getPhraseList(sPHRASE_LIST_FILE))
            // Only add non-empty phrases to the list.
            if (phrase.length() > 0)
                mPhrasesToFind.add(phrase);

        // Create/run an object to search for phrases concurrently.
        new SearchOneShotThreadJoin(mInputList,
                                    mPhrasesToFind).run();

        System.out.println("Ending SearchStream");
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
         * The list of phrases to find.
         */
        private final List<String> mPhrasesToFind;

        /**
         * The list of worker threads that were created.
         */
        private final List<Thread> mWorkerThreads;
        
        /**
         * The constructor initializes the fields.
         */
        SearchOneShotThreadJoin(List<String> inputList,
                                List<String> phrasesToFind) {
            // Initialize field.
            mPhrasesToFind = phrasesToFind;

            // Call the makeWorkerThreads() factory method to create a
            // list of threads that each runs the processInput()
            // method reference.  These threads will be joined after
            // they process the input strings.
            mWorkerThreads =
                makeWorkerThreads(this::processInput,
                                  inputList);
        }

        /**
         * This factory method creates a list of threads that will be
         * joined when their processing is done.
         *
         * @param task Function to run in each thread.
         * @param inputList List of strings to search.
         * @return List of threads that will run the @a task.
         */
        List<Thread> makeWorkerThreads(Function<String, Void> task,
                                       List<String> inputList) {
            // Create a new list.
            List<Thread> workerThreads = new ArrayList<>();

            // Create a thread for each input string to perform
            // processing designated by the task parameter.
            for (String string : inputList)
                workerThreads.add
                    (new Thread(()
                                // Create lambda runnable to run in thread.
                                ->
                                // Apply the task to process the input
                                // string.
                                task.apply(string)));

            return workerThreads;
        }

        /**
         * Start the threads to perform the concurrent phrase searches.
         */
        @Override
        public void run() {
            // Iterate through the list of threads and pass a method
            // reference that starts a thread for each input string.
            mWorkerThreads.forEach(Thread::start);

            // Iterate through the threads and pass the Thread.join()
            // method reference as a barrier synchronizer to wait for
            // all threads to finish.  Note how rethrowConsumer()
            // converts a checked exception to an unchecked exception.
            mWorkerThreads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            /*
              // This alternative, (more verbose) solution uses a
              // lambda expression instead of rethrowConsumer():

              mWorkerThreads.forEach(thread -> {
                                     try {
                                         thread.join();
                                     } catch (InterruptedException e) {
                                       throw new RuntimeException(e);
                                     }});
            */
        }

        /**
         * This method runs in a background thread and searches the @a
         * inputData for all occurrences of the phrases to find.
         */
        private Void processInput(String inputData) {
            String title = getTitle(inputData);

            // Iterate through each phrase to find.
            for (String phrase : mPhrasesToFind) 

                // Check to see how many times (if any) the phrase
                // appears in the input data.
                for (int i = inputData.indexOf(phrase, 0);
                     i != -1;
                     i = inputData.indexOf(phrase, i + phrase.length()))

                    // Whenever a match is found the processResults()
                    // method is called to handle the results.
                    System.out.println("in thread " 
                                       + Thread.currentThread().getId()
                                       + " the phrase \""
                                       + phrase
                                       + "\" was found at character offset "
                                       + i
                                       + " in \""
                                       + title
                                       + "\"");
            return null;
        }

        /**
         * Return the title portion of the @a inputData.
         */
        String getTitle(String inputData) {
            int endOfTitlePos = inputData.indexOf('\n');
            return inputData.substring(0, endOfTitlePos);
        }
    }


}

