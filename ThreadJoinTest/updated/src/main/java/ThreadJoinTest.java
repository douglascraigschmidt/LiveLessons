import utils.ExceptionUtils;
import utils.TestDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This program implements an "embarrassingly parallel" app that
 * searches for phrases in a {@link List} of {@link String} objects,
 * each containing a different work of William Shakespeare.  It
 * demonstrates the use of modern Java functional programming features
 * (such as lambda expressions, method references, and functional
 * interfaces) in conjunction with {@code Thread.start()} to run
 * threads and {@code Thread.join()} to wait for all running threads.
 *
 * This test starts a {@link Thread} for each input {@link String} and
 * uses {@code Thread.join()} to wait for all threads to finish.  This
 * implementation requires no Java synchronization mechanisms other
 * than what's provided by the Java {@link Thread} class.
 */
public class ThreadJoinTest {
    /**
     * A file containing the complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A {@link List} of phrases to search for in the complete works
     * of Shakespeare.
     */
    private static final String sPHRASE_LIST_FILE =
        "phraseList.txt";

    /**
     * The {@link List} of {@link String} objects, each of which
     * contains a work of Shakespeare.
     */
    private static final List<String> mInputList = TestDataFactory
        .getInput(sSHAKESPEARE_DATA_FILE, "@");

    /**
     * The {@link List} of phrases to search for in the works.
     */
    private static final List<String> mPhrasesToFind = TestDataFactory
        .getPhraseList(sPHRASE_LIST_FILE);
        
    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting ThreadJoinTest");

        // Create/run an object to search for phrases in parallel.
        new ThreadJoinTest();

        System.out.println("Ending ThreadJoinTest");
    }

    /**
     * Start the threads to perform the parallel phrase searches.
     */
    public ThreadJoinTest() {
        // Call the makeWorkerThreads() factory method to create a
        // list of threads that each runs the processInput() method
        // reference.  These threads will be joined after they process
        // the input strings.
        List<Thread> workerThreads =
            makeWorkerThreads(this::processInput);

        // Iterate through the list of threads and pass a method
        // reference that starts a thread for each input string.
        workerThreads.forEach(Thread::start);

        // This concise solution iterates through the threads and pass
        // the Thread.join() method reference as a barrier
        // synchronizer to wait for all threads to finish.  Note how
        // rethrowConsumer() converts a checked exception to an
        // unchecked exception.
        workerThreads
            .forEach(ExceptionUtils.rethrowConsumer(Thread::join));

        /*
        // This less concise solution iterates through the list of
        // threads and join with them all, which is a form of barrier
        // synchronization.
        workerThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }});
        */
    }

    /**
     * This factory method creates a {@link List} of Java {@link
     * Thread} objects that will be joined when their processing is
     * done.
     *
     * @param task {@link Function} to run in each {@link Thread}
     * @return {@link List} of {@link Thread} objects that run the
     *         {@code task}
     */
    List<Thread> makeWorkerThreads(Function<String, Void> task) {
        // Create a new list.
        List<Thread> workerThreads = new ArrayList<>();

        // Ensure we've got non-null input!
        assert mInputList != null;

        // Create a Thread for each input string to perform processing
        // designated by the task parameter.
        mInputList.forEach
            (input -> workerThreads
             // Add a new Thread to the List.
             .add(new Thread(() ->
                             // Create lambda runnable to run in
                             // Thread and apply the task to process
                             // the input string.
                             task.apply(input))));

        return workerThreads;
    }

    /**
     * This method runs in a background thread and searches the {@code
     * input} for all occurrences of the phrases to find.
     *
     * @param input Input {@link String} to search for matching
     *              phrases
     * @return A {@link Void} so we can use this method in a {@link
     *         Function}
     */
    private Void processInput(String input) {
        // Get the title of the work.
        String title = getTitle(input);

        // Ensure we've got non-null input!
        assert mPhrasesToFind != null;

        // Iterate through each phrase we're searching for.
        for (String phrase : mPhrasesToFind) {

            // Check to see how many times (if any) the phrase appears
            // in the input data.
            for (int offset = input.indexOf(phrase);
                 offset != -1;
                 offset = input.indexOf(phrase, offset + phrase.length())) {

                // Whenever a match is found print the results.
                System.out.println("["
                                   + Thread.currentThread().getId()
                                   + "] the phrase \""
                                   + phrase
                                   + "\" was found at character offset "
                                   + offset
                                   + " in \""
                                   + title
                                   + "\"");
            }
        }

        return null;
    }

    /**
     * @return The title portion of the {@code input}
     */
    String getTitle(String input) {
        // Create a Matcher.
        Matcher m = Pattern
            // Compile a regex that matches only the first line in the
            // input since each title appears on the first line of the
            // work.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(input);

        // Extract the title.
        return m.find()
            // Return the title string if there's a match.
            ? m.group()

            // Return an empty string if there's no match.
            : "";
    }
}

