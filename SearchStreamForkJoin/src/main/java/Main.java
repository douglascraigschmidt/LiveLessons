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

        // Run the tests.
        runTest("String", mInputList, false);
        runTest("String", mInputList, true);
        runTest("SharedString", mSharedInput, false);
        runTest("SharedString", mSharedInput, true);
        runSequentialTest("String", mInputList, false);
        runSequentialTest("String", mInputList, true);
        runSequentialTest("SharedString", mSharedInput, false);
        runSequentialTest("SharedString", mSharedInput, true);

        System.out.println("Ending SearchStream");
    }

    /**
     * Warm up the fork-join pool to account for any
     * instruction/data caching effects.
     */
    private static void warmUpForkJoinPool() {
        // Use the common fork-join pool to search the input looking
        // for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
                        .invoke(new SearchWithForkJoinTask(mInputList,
                                                           mPhrasesToFind,
                                                           true));

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Run the test and print out the timing results.  The @a parallel
     * parameter indicates whether to run the spliterator concurrently
     * or not.
     */
    private static void runTest(String testName,
                                List<CharSequence> inputList,
                                boolean parallel) {
        // Record the start time.
        long startTime = System.nanoTime();

        // Use the common fork-join pool to search the input looking
        // for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
                        .invoke(new SearchWithForkJoinTask(inputList,
                                                           mPhrasesToFind,
                                                           parallel));

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        // Print the results.
        printResults("SearchWithForkJoin" + "(" + testName + "|" + (parallel ? "parallel)" : "sequential)"),
                     stopTime,
                     listOfListOfSearchResults);

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Run the test and print out the timing results.  The @a parallel
     * parameter indicates whether to run the spliterator concurrently
     * or not.
     */
    private static void runSequentialTest(String testName,
                                          List<CharSequence> inputList,
                                          boolean parallel) {
        // Record the start time.
        long startTime = System.nanoTime();

        // Search the input looking for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            new LinkedList<>();

        // Loop through each input string in the list.
        for (CharSequence input : inputList) {
            // Create a task that searches an input string for a list
            // of phrases.
            SearchForPhrasesTask task =
                new SearchForPhrasesTask(input,
                                         mPhrasesToFind,
                                         parallel);

            listOfListOfSearchResults.add(task.compute());
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        // Print the results.
        printResults("SearchWithSequentialForkJoin" + "(" + testName + "|" + (parallel ? "parallel)" : "sequential)"),
                     stopTime,
                     listOfListOfSearchResults);

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Print out the search results.
     */
    private static void printResults(String testName,
                                     long stopTime,
                                     List<List<SearchResults>> listOfListOfSearchResults) {
        // Print the number of times each phrase matched the input.
        System.out.println("The search returned = "
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
