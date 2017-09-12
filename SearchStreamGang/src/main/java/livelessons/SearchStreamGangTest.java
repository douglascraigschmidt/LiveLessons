package livelessons;

import livelessons.streamgangs.*;
import livelessons.utils.Options;
import livelessons.utils.RunTimer;
import livelessons.utils.SearchResults;
import livelessons.utils.TestDataFactory;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * This test driver showcases how implementation strategies customize
 * the SearchStreamGang framework with different Java 8 mechanisms to
 * implement an "embarrassingly parallel" program that searches for
 * phases in a list of input strings.
 */
public class SearchStreamGangTest {
    /**
     * Enumerate all the implementation strategies to run.
     */
    enum TestsToRun {
        RXJAVA_PHASES,
        RXJAVA_INPUTS,
        SEQUENTIAL_LOOPS,
        SEQUENTIAL_STREAM,
        PARALLEL_STREAM_INPUTS,
        PARALLEL_STREAM_PHASES,
        PARALLEL_STREAMS,
        PARALLEL_SPLITERATOR,
        FORK_JOIN,
        COMPLETABLE_FUTURES_PHASES,
        COMPLETABLE_FUTURES_INPUTS
    }

    /**
     * Factory method that creates the desired type of
     * SearchStreamGang subclass implementation strategy.
     */
    private static SearchStreamGang makeSearchStreamGang(List<String> phraseList,
                                                         List<List<CharSequence>> inputData,
                                                         TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_LOOPS:
            return new SearchWithSequentialLoops(phraseList,
                        inputData);
        case SEQUENTIAL_STREAM:
            return new SearchWithSequentialStreams(phraseList,
                                                  inputData);
        case FORK_JOIN:
            return new SearchWithForkJoin(phraseList,
                                          inputData);
        case PARALLEL_SPLITERATOR:
            return new SearchWithParallelSpliterator(phraseList,
                                                     inputData);
        case PARALLEL_STREAM_INPUTS:
            return new SearchWithParallelStreamInputs(phraseList,
                                                      inputData);
        case PARALLEL_STREAM_PHASES:
            return new SearchWithParallelStreamPhrases(phraseList,
                                                     inputData);
        case PARALLEL_STREAMS:
            return new SearchWithParallelStreams(phraseList,
                                                 inputData);
        case COMPLETABLE_FUTURES_PHASES:
            return new SearchWithCompletableFuturesPhrases(phraseList,
                                                         inputData);
        case COMPLETABLE_FUTURES_INPUTS:
            return new SearchWithCompletableFuturesInputs(phraseList,
                                                          inputData);
        case RXJAVA_INPUTS:
            return new SearchWithRxJavaInputs(phraseList, inputData);
        case RXJAVA_PHASES:
            return new SearchWithRxJavaPhrases(phraseList, inputData);
        }
        return null;
    }

    /*
     * Input files.
     */
    /**
     * The complete works of William Shakespeare.
     */
    private static String sSHAKESPEARE_DATA_FILE =
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
    public static void main(String[] args) throws Throwable {
        System.out.println("Starting SearchStreamGangTest");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // All the input is initialized here.
        List<List<CharSequence>> inputData = 
            new ArrayList<List<CharSequence>>() {
                { // Create a list of input from the complete works of
                  // William Shakespeare.
                  add(TestDataFactory
                      // Split input by input separator from Options singleton.
                      .getSharedInput(sSHAKESPEARE_DATA_FILE,
                                      Options.getInstance().getInputSeparator()));
                }
            };

        // Get the list of input phases to find.
        List<String> phaseList = 
            TestDataFactory.getPhraseList(sPHASE_LIST_FILE);

        // Create/run StreamGangs to search for the phases to find.
        runTests(phaseList,
                 inputData);

        System.out.println("Ending SearchStreamGangTest");
    }

    /**
     * Create/run appropriate type of StreamGang to search for phases.
     */
    private static void runTests(List<String> phaseList,
                                 List<List<CharSequence>> inputData) {
        // Warm up the fork-join pool.
        warmUpForkJoinPool(phaseList, inputData);

        // Run all the SearchStreamGang tests.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test);

            // Use the factory method to make the appropriate
            // SearchStreamGang.
            SearchStreamGang streamGang =
                makeSearchStreamGang(phaseList,
                                     inputData,
                                     test);

            // Ensure nothing weird happened..
            assert streamGang != null;

            // Execute the test.
            streamGang.run();

            // Run the garbage collector to free up memory and
            // minimize timing perturbations on each test.
            System.gc();

            System.out.println("Ending " + test);
        }

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

