
/**
 * @class TaskGangTest
 *
 * @brief This program tests various subclassses of the TaskGang
 *        framework, which use different Java concurrency and
 *        synchronization mechanisms to implement an "embarraassingly
 *        parallel" application that concurrently searches for words
 *        in a List of Strings.
 *
 * @@ NS: Need to improve the documentation.
 */
public class TaskGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        ONESHOT_THREAD_PER_TASK,
        ONESHOT_EXECUTOR_SERVICE,
        CYCLIC_EXECUTOR_SERVICE,
        ONESHOT_EXECUTOR_SERVICE_FUTURE,
        ONESHOT_EXECUTOR_COMPLETION_SERVICE
    }

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true
        ;
    // @@ NS: Need to get this data from files rather than from
    // hard-coded strings!

    /**
     * This input array is used by the one-shot tests that search for
     * the words concurrently in multiple threads.
     */
    private final static String[][] mOneShotInputStrings = {
        {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a fixed number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[][] mFixedNumberOfInputStrings = {
        {"xdoodoo", "xreo", "xmiomio", "xfao", "xsoosoo", "xlao", "xtiotio", "xdoo"},
        {"xdoo", "xreoreo", "xmio", "xfaofao", "xsoo", "xlaolao", "xtio", "xdoodoo"}
    };

    /**
     * This input array is used by the cyclic test that continues to
     * search a variable number of words/Threads concurrently until
     * there's no more input to process.
     */
    private final static String[][] mVariableNumberOfInputStrings = {
        {"xfaofao"},
        {"xsoo", "xlaolao", "xtio", "xdoodoo"},
        {"xdoo", "xreoreo"},
        {"xreoreo", "xdoo"},
        {"xdoodoo", "xreo", "xmiomio"}
    };

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
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Factory method that creates the desired type of
     * SearchTaskGangCommon subclass implementation.
     */
    private static SearchTaskGangCommon makeTaskGang(String[] wordList,
                                                     TestsToRun choice) {
        switch(choice) {
        case ONESHOT_THREAD_PER_TASK:
            return new OneShotThreadPerTask(wordList,
                                            mOneShotInputStrings);
        case ONESHOT_EXECUTOR_SERVICE:
            return new OneShotExecutorService(wordList,
                                              mOneShotInputStrings);
        case CYCLIC_EXECUTOR_SERVICE:
            return new CyclicExecutorService(wordList,
                                             mFixedNumberOfInputStrings);
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
