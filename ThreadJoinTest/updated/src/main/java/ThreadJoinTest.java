import utils.TestDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This program implements an "embarrassingly parallel" app that
 * searches for phrases in a list of strings containing the complete
 * works of William Shakespeare.  It demonstrates the use of Java
 * functional programming features (such as lambda expressions, method
 * references, and functional interfaces) in conjunction with
 * Thread.start() to run threads and Thread.join() to wait for all
 * running threads.
 */
public class ThreadJoinTest {
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
    private static final List<String> mInputList =
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE, "@");

    /**
     * The list of phrases to find.
     */
    private static final List<String> mPhrasesToFind =
            TestDataFactory.getPhraseList(sPHRASE_LIST_FILE);
        
    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting ThreadJoinTest");

        // Create/run an object to search for phrases in parallel.
        new SearchOneShotThreadJoin();

        System.out.println("Ending ThreadJoinTest");
    }

    /**
     * This class starts a thread for each element in the list of
     * input strings and uses Thread.join() to wait for all threads to
     * finish.  This implementation requires no Java synchronization
     * mechanisms other than what's provided by the Java Thread class.
     */
    static class SearchOneShotThreadJoin {
        /**
         * Start the threads to perform the parallel phrase searches.
         */
        SearchOneShotThreadJoin() {
            // Call the makeWorkerThreads() factory method to create a
            // list of threads that each runs the processInput()
            // method reference.  These threads will be joined after
            // they process the input strings.
            // Could use List<Thread>
            var workerThreads = makeWorkerThreads(this::processInput);

            // Iterate through the list of threads and pass a method
            // reference that starts a thread for each input string.
            workerThreads.forEach(Thread::start);

            // Iterate through the list of threads and join with them
            // all, which is a form of barrier synchronization.
            workerThreads.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }});

            /*
            // This alternative, (more concise) solution iterate
            // through the threads and pass the Thread.join() method
            // reference as a barrier synchronizer to wait for all
            // threads to finish.  Note how rethrowConsumer() converts
            // a checked exception to an unchecked exception.
            workerThreads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));
            */
        }

        /**
         * This factory method creates a list of threads that will be
         * joined when their processing is done.
         *
         * @param task Function to run in each thread.
         * @return List of threads that will run the @a task.
         */
        List<Thread> makeWorkerThreads(Function<String, Void> task) {
            // Create a new list.
            // Can also use List<Thread>
            var workerThreads = new ArrayList<Thread>();

            // Ensure we've got non-null input!
            assert mInputList != null;

            // Create a thread for each input string to perform
            // processing designated by the task parameter.
            mInputList.forEach
                (input ->
                 // Add a new thread to the list.
                 workerThreads.add(new Thread(()
                                              // Create lambda
                                              // runnable to run in
                                              // thread.
                                              ->
                                              // Apply the task to
                                              // process the input
                                              // string.
                                              task.apply(input))));

            return workerThreads;
        }

        /**
         * This method runs in a background thread and searches the @a
         * input for all occurrences of the phrases to find.
         *
         * @param input Input string to search for matching phrases.
         */
        private Void processInput(String input) {
            // Get the title of the work.
            // Could use String title
            var title = getTitle(input);

            // Ensure we've got non-null input!
            assert mPhrasesToFind != null;

            // Iterate through each phrase to find.
            for (var phrase : mPhrasesToFind)

                // Check to see how many times (if any) the phrase
                // appears in the input data.
                for (int offset = input.indexOf(phrase);
                     offset != -1;
                     offset = input.indexOf(phrase, offset + phrase.length()))

                    // Whenever a match is found we print out the
                    // results.
                    System.out.println("in thread " 
                                       + Thread.currentThread().getId()
                                       + " the phrase \""
                                       + phrase
                                       + "\" was found at character offset "
                                       + offset
                                       + " in \""
                                       + title
                                       + "\"");
            return null;
        }

        /**
         * Return the title portion of the @a input.
         */
        String getTitle(String input) {
            // Each title appears on the first line of a work.
            int endOfTitlePos = input.indexOf('\n');

            // Extract the title.
            return input.substring(0, endOfTitlePos);
        }
    }
}
