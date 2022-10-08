package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.Options;
import livelessons.utils.RunTimer;
import livelessons.utils.TestDataFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different modern Java
 * mechanisms to implement an "embarrassingly parallel" program that
 * searches for famous Bard phases in a list of input strings
 * containing the complete works of William Shakespeare.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        WARMUP_FORK_JOIN_THREAD_POOL,
        FORK_JOIN,
        PARALLEL_STREAMS,
        PARALLEL_SPLITERATOR,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_PHRASES,
        COMPLETABLE_FUTURES_INPUTS,
        COMPLETABLE_FUTURES_PHRASES,
        REACTOR,
        REACTOR_INPUTS,
        REACTOR_PHRASES,
        RXJAVA,
        RXJAVA_INPUTS,
        RXJAVA_PHRASES,
        SEQUENTIAL_LOOPS,
        SEQUENTIAL_STREAM
    }

    /**
     * Maps each of the TestsToRun to the associated SearchStreamGang
     * strategy that implements this test.  A LinkedHashMap is used to
     * ensure the tests run in the same order as the enumerals in the
     * TestsToRun enum.
     */
    private static final Map<TestsToRun, SearchStreamGang> sSTRATEGY_MAP =
        new LinkedHashMap<>();

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A list of Bard phrases to search for in the complete works of
     * Shakespeare.
     */
    private static final String sPHASE_LIST_FILE =
        "phraseList.txt";

    /**
     * This is the entry point into the test driver program.
     */
    public static void main(String[] args) {
        System.out.println("Starting SearchStreamGangTest");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // All the input is initialized here.
        List<List<CharSequence>> inputData = 
            new ArrayList<>() {{
                // Create a list of input from the complete works of
                // William Shakespeare.
                add(TestDataFactory
                    // Split input via Options singleton separator.
                    .getSharedInput(sSHAKESPEARE_DATA_FILE, Options
                                    .getInstance()
                                    .getInputSeparator()));
            }};

        // Get the list of famous Bard phases to find.
        List<String> phraseList =
            TestDataFactory.getPhraseList(sPHASE_LIST_FILE);

        // Initialize the map with all the implementation strategies.
        makeStrategyMap(phraseList, inputData);

        // Run all the SearchStreamGang implementation strategies to
        // search for Bard phrases.
        runTests();

        System.out.println("Ending SearchStreamGangTest");
    }

    /**
     * Run all the SearchStreamGang implementation strategies to
     * search for Bard phrases.
     */
    private static void runTests() {
        // Run all the SearchStreamGang tests.
        sSTRATEGY_MAP
            .forEach((test, searchStreamGang) -> {
                    System.out.println("Starting " + test);

                    // Run the garbage collector to free up memory and
                    // minimize timing perturbations on each test.
                    System.gc();

                    // Execute the test.
                    searchStreamGang.run();

                    System.out.println("Ending " + test);
                });

        // Sort and display all the timing results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Factory method that initializes the {@code sSTRATEGY_MAP} with
     * all the SearchStreamGang subclass implementation strategies.
     */
    private static void makeStrategyMap(List<String> phraseList,
                                        List<List<CharSequence>> inputData) {
        // Initialize sSTRATEGY_MAP.
        for (TestsToRun test : TestsToRun.values()) {
            switch (test) {
                case WARMUP_FORK_JOIN_THREAD_POOL, FORK_JOIN -> sSTRATEGY_MAP.put(test, new SearchWithForkJoin(phraseList, inputData));
                case SEQUENTIAL_LOOPS -> sSTRATEGY_MAP.put(test, new SearchWithSequentialLoops(phraseList,
                                                                                               inputData));
                case SEQUENTIAL_STREAM -> sSTRATEGY_MAP.put(test, new SearchWithSequentialStreams(phraseList,
                                                                                                  inputData));
                case PARALLEL_SPLITERATOR -> sSTRATEGY_MAP.put(test, new SearchWithParallelSpliterator(phraseList,
                                                                                                       inputData));
                case PARALLEL_STREAM_INPUTS -> sSTRATEGY_MAP.put(test, new SearchWithParallelStreamInputs(phraseList,
                                                                                                          inputData));
                case PARALLEL_STREAM_PHRASES -> sSTRATEGY_MAP.put(test, new SearchWithParallelStreamPhrases(phraseList,
                                                                                                            inputData));
                case PARALLEL_STREAMS -> sSTRATEGY_MAP.put(test, new SearchWithParallelStreams(phraseList,
                                                                                               inputData));
                case COMPLETABLE_FUTURES_PHRASES -> sSTRATEGY_MAP.put(test, new SearchWithCompletableFuturesPhrases(phraseList,
                                                                                                                    inputData));
                case COMPLETABLE_FUTURES_INPUTS -> sSTRATEGY_MAP.put(test, new SearchWithCompletableFuturesInputs(phraseList,
                                                                                                                  inputData));
                case REACTOR -> sSTRATEGY_MAP.put(test, new SearchWithReactor(phraseList,
                                                                              inputData));
                case REACTOR_INPUTS -> sSTRATEGY_MAP.put(test, new SearchWithReactorInputs(phraseList,
                                                                                           inputData));
                case REACTOR_PHRASES -> sSTRATEGY_MAP.put(test, new SearchWithReactorPhrases(phraseList,
                                                                                             inputData));
                case RXJAVA -> sSTRATEGY_MAP.put(test, new SearchWithRxJava(phraseList,
                                                                            inputData));
                case RXJAVA_INPUTS -> sSTRATEGY_MAP.put(test, new SearchWithRxJavaInputs(phraseList,
                                                                                         inputData));
                case RXJAVA_PHRASES -> sSTRATEGY_MAP.put(test, new SearchWithRxJavaPhrases(phraseList,
                                                                                           inputData));
            }
        }
    }
}

