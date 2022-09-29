
/**
 * @class TaskGangTest
 *
 * @brief This test driver showcases how various subclasses customize
 *        the TaskGang framework with different Java concurrency and
 *        synchronization mechanisms to implement an "embarrassingly
 *        parallel" application that concurrently searches for words
 *        in a List of input Strings.
 */
public class TaskGangTest {
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
     * Array of words to search for in the input.
     */
    private final static String[] mWordList = {"do",
                                               "re",
                                               "mi",
                                               "fa",
                                               "so",
                                               "la",
                                               "ti",
                                               "do"};
        
    /**
     * This input array is used by the one-shot tests that search for
     * the words concurrently in multiple threads.
     */
    private final static String[][] mOneShotInputStrings = {
        {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
    };

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true;

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Factory method that creates the desired type of TaskGang
     * subclass implementation.
     */
    private static Runnable makeTaskGang(String[] wordList,
                                         TestsToRun choice) {
        switch(choice) {
        case ONESHOT_THREAD_PER_TASK:
            return new OneShotThreadPerTask(wordList,
                                            mOneShotInputStrings);
        case ONESHOT_EXECUTOR_SERVICE:
            return new OneShotExecutorService(wordList,
                                              mOneShotInputStrings);
        case ONESHOT_EXECUTOR_SERVICE_FUTURE:
            return new OneShotExecutorServiceFuture(wordList,
                                                    mOneShotInputStrings);
        case ONESHOT_EXECUTOR_COMPLETION_SERVICE:
            return new OneShotExecutorCompletionService(wordList,
                                                        mOneShotInputStrings);
        }
        return null;
    }
    
    /**
     * This is the entry point into the test program.  
     */
    public static void main(String[] args) {
        printDebugging("Starting TaskGangTest");
        
        // Create/run appropriate type of SearchTaskGang to search for
        // words concurrently.

        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting "
                           + test);
            makeTaskGang(mWordList, test).run();
            printDebugging("Ending "
                           + test);
        }

        printDebugging("Ending TaskGangTest");
    }
}
