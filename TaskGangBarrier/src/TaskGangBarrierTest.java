import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
// import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * @class TaskGangBarrierTest
 *
 * @brief This program tests various subclassses of the TaskGang
 *        framework that use different Java barrier synchronizers to
 *        implement an "embarraassingly parallel" application that
 *        concurrently searches for words in a List of Strings.
 */
public class TaskGangBarrierTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        COUNTDOWNLATCH,
        CYCLIC,
        PHASER
    }

    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = true
;
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
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Factory method that creates the desired type of
     * SearchTaskGangCommon subclass implementation.
     */
    private static SearchTaskGangCommon 
        makeTaskGang(String[] wordList,
                     TestsToRun choice) {
        switch(choice) {
        case COUNTDOWNLATCH:
            return new OneShotSearchWithCountDownLatch(wordList,
                                                      mOneShotInputStrings);
        case CYCLIC:
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
        printDebugging("Starting TaskGangBarrierTest");
        
        // Create/run appropriate type of TaskGang to search for words
        // concurrently.

        for (TestsToRun test : TestsToRun.values()) {
            printDebugging("Starting "
                           + test);
            makeTaskGang(mWordList, test).run();
            printDebugging("Ending "
                           + test);
        }

        printDebugging("Ending TaskGangBarrierTest");
    }
}
