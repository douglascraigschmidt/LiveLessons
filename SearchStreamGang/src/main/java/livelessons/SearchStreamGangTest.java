package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.Options;
import livelessons.utils.RunTimer;
import livelessons.utils.SearchResults;
import livelessons.utils.TestDataFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different modern Java
 * mechanisms to implement an "embarrassingly parallel" program that
 * searches for phases in a list of input strings.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        COMPLETABLE_FUTURES_INPUTS,
        COMPLETABLE_FUTURES_PHASES,
        FORK_JOIN,
        PARALLEL_SPLITERATOR,
        PARALLEL_STREAMS,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_PHASES,
        RXJAVA_INPUTS,
        RXJAVA_PHASES,
        SEQUENTIAL_LOOPS,
        SEQUENTIAL_STREAM
    }

    /**
     * Maps each of the TestsToRun to the associated SearchStreamGang strategy that
     * implements this test.
     */
    private static final Map<TestsToRun, SearchStreamGang> sSTRATEGY_MAP =
        new LinkedHashMap<>();

    /**
     * Factory method that initializes the {@code sSTRATEGY_MAP} with
     * all the SearchStreamGang subclass implementation strategies.
     */
    private static void makeStrategyMap(List<String> phraseList,
                                        List<List<CharSequence>> inputData) {
        // Initialize sSTRATEGY_MAP.
        for (TestsToRun test : TestsToRun.values()) {
            switch (test) {
            case SEQUENTIAL_LOOPS:
                sSTRATEGY_MAP.put(test, new SearchWithSequentialLoops(phraseList,
                                                                      inputData));
                break;
            case SEQUENTIAL_STREAM:
                sSTRATEGY_MAP.put(test, new SearchWithSequentialStreams(phraseList,
                                                                        inputData));
                break;
            case FORK_JOIN:
                sSTRATEGY_MAP.put(test, new SearchWithForkJoin(phraseList,
                                                               inputData));
                break;
            case PARALLEL_SPLITERATOR:
                sSTRATEGY_MAP.put(test, new SearchWithParallelSpliterator(phraseList,
                                                                          inputData));
                break;
            case PARALLEL_STREAM_INPUTS:
                sSTRATEGY_MAP.put(test, new SearchWithParallelStreamInputs(phraseList,
                                                                           inputData));
                break;
            case PARALLEL_STREAM_PHASES:
                sSTRATEGY_MAP.put(test, new SearchWithParallelStreamPhrases(phraseList,
                                                                            inputData));
                break;
            case PARALLEL_STREAMS:
                sSTRATEGY_MAP.put(test, new SearchWithParallelStreams(phraseList,
                                                                      inputData));
                break;
            case COMPLETABLE_FUTURES_PHASES:
                sSTRATEGY_MAP.put(test, new SearchWithCompletableFuturesPhrases(phraseList,
                                                                                inputData));
                break;
            case COMPLETABLE_FUTURES_INPUTS:
                sSTRATEGY_MAP.put(test, new SearchWithCompletableFuturesInputs(phraseList,
                                                                               inputData));
                break;
            case RXJAVA_INPUTS:
                sSTRATEGY_MAP.put(test, new SearchWithRxJavaInputs(phraseList,
                                                                   inputData));
                break;
            case RXJAVA_PHASES:
                sSTRATEGY_MAP.put(test, new SearchWithRxJavaPhrases(phraseList,
                                                                    inputData));
                break;
            }
        }
    }

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
    private static String sPHASE_LIST_FILE =
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
            new ArrayList<>() { {
                // Create a list of input from the complete works of
                // William Shakespeare.
                add(TestDataFactory
                    // Split input via Options singleton separator.
                    .getSharedInput(sSHAKESPEARE_DATA_FILE, Options
                                    .getInstance().getInputSeparator()));
            }};

        // Get the list of input phases to find.
        List<String> phaseList = 
            TestDataFactory.getPhraseList(sPHASE_LIST_FILE);

        // Create/run StreamGangs to search for the phases to find.
        runTests(phaseList,
                 inputData);

        System.out.println("Ending SearchStreamGangTest");
    }

    /**
     * Create/run appropriate type of StreamGang to search for phrases.
     */
    private static void runTests(List<String> phraseList,
                                 List<List<CharSequence>> inputData) {
        // Warm up the fork-join pool.
        warmUpForkJoinPool(phraseList, inputData);

        // Initialize the map.
        makeStrategyMap(phraseList, inputData);

        // Run all the SearchStreamGang tests.
        sSTRATEGY_MAP.forEach((test, searchStreamGang) -> {
                System.out.println("Starting " + test);

                // Execute the test.
                searchStreamGang.run();

                // Run the garbage collector to free up memory and
                // minimize timing perturbations on each test.
                System.gc();

                System.out.println("Ending " + test);
            });

        // Sort and display all the timing results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warm up the threads in the fork-join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool(List<String> phraseList,
                                           List<List<CharSequence>> inputData) {
        System.out.println("Warming up the fork-join pool");
        // Create a SearchStreamGang that's used to find the # of
        // times each phrase in phraseList appears in the inputData.
        SearchStreamGang streamGang =
            new SearchStreamGang(phraseList,
                                 inputData);
 
        inputData
            // Process the stream of input strings in parallel.
            .parallelStream()
 
            // Iterate for each array of input strings.
            .forEach(listOfStrings -> {
                    // The results are stored in a list of input streams,
                    // where each input string is associated with a list
                    // of SearchResults corresponding to phrases that
                    // matched the input string.
                    List<SearchResults> listOfSearchResults = listOfStrings
                        // Process the stream of input data in parallel.
                        .parallelStream()
 
                        // Concurrently search each input string for all
                        // occurrences of the phrases to find.
                        .map(inputString -> {
                                // Get the section title.
                                String title = streamGang.getTitle(inputString);
 
                                // Skip over the title.
                                CharSequence input = inputString.subSequence(title.length(),
                                                                             inputString.length());
 
                                return phraseList
                                // Process the stream of phrases in parallel.
                                .parallelStream()
 
                                // Search for all places in the input
                                // String where the phrase appears and
                                // return a SearchResults object.
                                .map(phrase ->
                                     streamGang.searchForPhrase(phrase,
                                                                input,
                                                                title,
                                                                true))
 
                                // Only keep a result that has at least one match.
                                .filter(not(SearchResults::isEmpty))
 
                                // Collect a list of SearchResults for
                                // each phrase that matched this input
                                // string.
                                .collect(toList());
                            })
 
                        // Flatten the stream of lists of SearchResults
                        // into a stream of SearchResults.
                        .flatMap(List::stream)
 
                        // Collect a list of containing SearchResults
                        // for each input string.
                        .collect(toList());
 
                    // Determine how many word matches we obtained.
                    // SearchResults::print();
                    int totalWordsMatched = listOfSearchResults
                        .stream()
 
                        // Compute the total number of times each word
                        // matched the input string.
                        .mapToInt(SearchResults::size)
 
                        // Sum the results.
                        .sum();
                        
                    System.out.println("warmUpForkJoinPool"
                                       + ": The search returned = "
                                       + totalWordsMatched
                                       + " word matches for "
                                       + listOfStrings.size()
                                       + " input strings");
                });

        // Run the garbage collector to free up memory and
        // minimize timing perturbations on each test.
        System.gc();
    }
}

