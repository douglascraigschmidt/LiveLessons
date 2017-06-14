import search.SearchForPhrasesTask;
import search.SearchResults;
import search.SearchWithForkJoinTask;
import utils.Options;
import utils.TestDataFactory;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static java.util.stream.Collectors.groupingBy;

/**
 * This example implements an "embarrassingly parallel" program that
 * uses a Java 7 fork-join pool to concurrently search for phrases in
 * a list of input containing all the works of Shakespeare.
 */
public class Main {
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
     * A List of Strings containing the complete works of Shakespeare.
     */
    private static List<CharSequence> mInputList;

    /**
     * A List of SharedStrings containing the complete works of
     * Shakespeare.
     */
    private static List<CharSequence> mSharedInput;

    /**
     * The list of phrases to find.
     */
    private static List<String> mPhrasesToFind;
        
    /**
     * Keep track of which SearchStreamSpliterator performed the best.
     */
    private static Map<Long, String> mResultsMap =
        new HashMap<>();

    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // Create a list of Strings to search from the complete works
        // of William Shakespeare.
        mInputList =
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     // Split input by input separator
                                     // from Options singleton.
                                     Options.getInstance().getInputSeparator());

        // Create a list of SharedStrings to search from the complete
        // works of William Shakespeare.
        mSharedInput = 
            TestDataFactory.getSharedInput(sSHAKESPEARE_DATA_FILE,
                                           // Split input by input
                                           // separator from Options
                                           // singleton.
                                           Options.getInstance().getInputSeparator());

        // Get the list of phrases to find in the works of
        // Shakespeare.
        mPhrasesToFind = TestDataFactory
            .getPhraseList(sPHRASE_LIST_FILE);

        // Warm up the fork-join pool to account for any
        // instruction/data caching effects.
        warmUpForkJoinPool();

        // Run the non-shared string tests.
        runTest(mInputList, false, false, false, false);
        runTest(mInputList, false, true, true, true);

        // Run the non-shared string tests.
        runTest(mSharedInput, true, false, false, false);
        runTest(mSharedInput, true, false, true, false);
        runTest(mSharedInput, true, false, false, true);
        runTest(mSharedInput, true, false, true, true);
        runTest(mSharedInput, true, true, false, false);
        runTest(mSharedInput, true, true, true, false);
        runTest(mSharedInput, true, true, false, true);
        runTest(mSharedInput, true, true, true, true);

        // Print out the search results.
        printResults();

        System.out.println("Ending SearchStream");
    }

    /**
     * Warm up the fork-join pool to account for any
     * instruction/data caching effects.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("Warming up the fork-join pool");

        @SuppressWarnings("unused")
        // Search the input looking for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
                        .invoke(new SearchWithForkJoinTask(mInputList,
                                                           mPhrasesToFind,
                                                           true,
                                true,
                                true));

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Run the test and print out the timing results.  The various @a
     * parallel* parameters indicates whether to run different parts
     * of the solution in parallel or not.
     */
    private static void runTest(List<CharSequence> inputList,
                                boolean sharedString,
                                boolean parallelSearching,
                                boolean parallelPhrases,
                                boolean parallelInput) {
        // Record the start time.
        long startTime = System.nanoTime();

        // Use the common fork-join pool to search the input looking
        // for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
                        .invoke(new SearchWithForkJoinTask(inputList,
                                                           mPhrasesToFind,
                                                           parallelSearching,
                                                           parallelPhrases,
                                                           parallelInput));

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        // Store the results.
        storeResults("SearchWithForkJoin("
                     + (sharedString ? "shared-string" : "string")
                     + "|"
                     + (parallelSearching ? "parallel" : "sequential")
                     + "Spliterator|"
                     + (parallelPhrases ? "parallel" : "sequential")
                     + "Phrases|"
                     + (parallelInput ? "parallel" : "sequential")
                     + "Input)",
                     stopTime,
                     listOfListOfSearchResults);

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Print out the search results.
     */
    private static void printResults() {
        // Print out the contents of the mResultsMap in sorted order.
        mResultsMap
            // Get the entrySet for the mResultsMap.
            .entrySet()

            // Convert the entrySet into a stream.
            .stream()

            // Sort the stream by the timing results (key).
            .sorted(Map.Entry.comparingByKey())

            // Print all the entries in the sorted stream.
            .forEach(entry
                     -> System.out.println(entry.getValue()));

    }

    /**
     * Store the search results.
     */
    private static void storeResults(String testName,
                                     long stopTime,
                                     List<List<SearchResults>> listOfListOfSearchResults) {
        // Print the number of times each phrase matched the input.
        mResultsMap.put(stopTime,
                        "The search returned = "
                        // Count the number of matches.
                        + listOfListOfSearchResults.stream()
                        .mapToInt(list
                                  -> list.stream().mapToInt(SearchResults::size).sum())
                        .sum()
                        + " phrase matches for "
                        + mInputList.size()
                        + " input strings in "
                        + stopTime
                        + " milliseconds for "
                        + testName);

        // Print the matching titles.
        if (Options.getInstance().isVerbose())
            printTitles(listOfListOfSearchResults);
    }

    /**
     * Print the matching titles.
     */
    private static void printTitles(List<List<SearchResults>> listOfListOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found.
        Map<String, List<SearchResults>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Flatten the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a Map.
            .collect(groupingBy(SearchResults::getTitle));

        // Print out the results in the map, where each phrase is
        // first printed followed by a list of the indices where the
        // phrase appeared in the input.
        resultsMap.forEach((key, value)
                           -> { 
                               System.out.println("Title \""
                                                  + key
                                                  + "\" contained");
                               // Print out the indicates for this key.
                               value.forEach(SearchResults::print);
                           });
    }
}
