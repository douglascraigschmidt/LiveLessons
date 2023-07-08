import tasks.OneShotExecutorCompletionService;
import tasks.OneShotExecutorService;
import tasks.OneShotExecutorServiceFuture;
import tasks.OneShotThreadPerTask;
import utils.Options;
import utils.TaskGang;

import static utils.Options.*;

/**
 * This test driver showcases how various subclasses customize the
 * {@link TaskGang} framework with different Java concurrency and
 * synchronization mechanisms to implement an "embarrassingly
 * parallel" application that concurrently searches for words in a
 * {@link List} of input {@link String} objects.
 */
public class SearchTaskGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        ONESHOT_THREAD_PER_TASK,
        ONESHOT_EXECUTOR_SERVICE,
        ONESHOT_EXECUTOR_SERVICE_FUTURE,
        ONESHOT_EXECUTOR_COMPLETION_SERVICE
    }


    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        Options.instance().parseArgs(args);

        print("Starting TaskGangTest");

        // Iterate through all the tests.
        for (var test : TestsToRun.values()) {
            print("Starting " + test);

            // Create/run the appropriate type of SearchTaskGang to
            // search for words concurrently.
            makeTaskGang(sWordList, test).run();

            print("Ending " + test);
        }

        print("Ending TaskGangTest");
    }

    /**
     * Factory method that creates the desired type of {@link
     * TaskGang} subclass implementation.
     */
    private static Runnable makeTaskGang(String[] wordList,
                                         TestsToRun choice) {
        return switch (choice) {
        case ONESHOT_THREAD_PER_TASK ->
            new OneShotThreadPerTask(wordList,
                                     sOneShotInputStrings);
        case ONESHOT_EXECUTOR_SERVICE ->
            new OneShotExecutorService(wordList,
                                       sOneShotInputStrings);
        case ONESHOT_EXECUTOR_SERVICE_FUTURE ->
            new OneShotExecutorServiceFuture(wordList,
                                             sOneShotInputStrings);
        case ONESHOT_EXECUTOR_COMPLETION_SERVICE ->
            new OneShotExecutorCompletionService(wordList,
                                                 sOneShotInputStrings);
        };
    }
}
