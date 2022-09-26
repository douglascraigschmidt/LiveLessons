/**
 * This test driver showcases how various subclasses of the {@link
 * TaskGang} framework use different Java barrier synchronizers to
 * implement an "embarrassingly parallel" app that concurrently
 * searches for words in one or more Lists of input Strings.
 */
public class BarrierTaskGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        COUNTDOWNLATCH,
        CYCLIC_BARRIER,
        PHASER
    }

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true;

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
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
     * Used by the {@link OneShotSearchWithCountDownLatch} test that
     * searches for the words concurrently in multiple threads.
     */
    private final static String[][] mOneShotInputStrings = {
        {"xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo"}
    };

    /**
     * Used by the {@link CyclicSearchWithCyclicBarrier} test that
     * continues to search a fixed number of words/Threads
     * concurrently until there's no more input to process.
     */
    private final static String[][] mFixedNumberOfInputStrings = {
        {"xdoodoo", "xreo", "xmiomio", "xfao", "xsoosoo", "xlao", "xtiotio", "xdoo"},
        {"xdoo", "xreoreo", "xmio", "xfaofao", "xsoo", "xlaolao", "xtio", "xdoodoo"}
    };

    /**
     * Used by the {@link CyclicSearchWithPhaser} test that continues
     * to search a variable number of words/threads concurrently until
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
     * Factory method that creates the desired type of TaskGang
     * subclass implementation.
     */
    private static Runnable makeTaskGang(String[] wordList,
                                         TestsToRun choice) {
        switch(choice) {
        case COUNTDOWNLATCH:
            return new OneShotSearchWithCountDownLatch(wordList,
                                                       mOneShotInputStrings);
        case CYCLIC_BARRIER:
            return new CyclicSearchWithCyclicBarrier(wordList,
                                                     mFixedNumberOfInputStrings);
        case PHASER:
            return new CyclicSearchWithPhaser(wordList,
                                              mVariableNumberOfInputStrings);
        }
        return null;
    }
    
    /**
     * This is the entry point into the test program.  
     */
    static public void main(String[] args) {
        printDebugging("Starting BarrierTaskGangTest");
        
        // Iterate through all the tests.
        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting "
                           + test);

            // Create/run appropriate type of TaskGang to search for
            // words concurrently.
            makeTaskGang(mWordList, test).run();

            printDebugging("Ending "
                           + test);
        }

        printDebugging("Ending BarrierTaskGangTest");
    }
}
