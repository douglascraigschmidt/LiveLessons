import search.IndexAwareSearchWithForkJoinTask;
import search.SearchResults;
import search.SearchWithForkJoinTask;
import utils.Options;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static java.util.stream.Collectors.groupingBy;
import static utils.StreamsUtils.PentaFunction;

/**
 * This example implements an "embarrassingly parallel" program that
 * uses a modern Java fork-join pool and a "divide and conquer"
 * strategy (i.e., splitting various folders, phrases, and strings in
 * half) to search for phrases in a recursive directory folder
 * containing all the works of Shakespeare.  All parallel processing
 * in this program only uses "classic" Java 7 features (i.e., no Java
 * 8 parallel streams) to demonstrate "raw" fork-join pool
 * programming.
 */
public class Main {
    /*
     * Input files.
     */

    /**
     * The root of a recursive directory folder containing the
     * complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_FOLDER =
        "works";

    /**
     * A list of phrases to search for in the complete works of
     * Shakespeare.
     */
    private static final String sPHRASE_LIST_FILE =
        "phraseList.txt";

    /**
     * The list of phrases to find.
     */
    private static List<String> mPhrasesToFind;
        
    /**
     * Keep track of which implementation performed the best.
     */
    private static final Map<Long, String> mResultsMap =
        new HashMap<>();

    /**
     * Customize the PentaFunction for the SearchWithForkJoinTask
     * hierarchy of classes so we can use constructor references.
     */
    private interface SearchWithForkJoinTaskFactory
        extends PentaFunction<List<CharSequence>,
                              List<String>,
                              Boolean,
                              Boolean,
                              Boolean,
                              SearchWithForkJoinTask> {}

    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("Starting SearchStream");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // Get the list of phrases to find in the works of
        // Shakespeare.
        mPhrasesToFind = TestDataFactory
            .getPhraseList(sPHRASE_LIST_FILE);

        // This constructor reference creates an
        // IndexAwareSearchWithForkJoinTask object.
        SearchWithForkJoinTaskFactory indexAwareConsRef =
            IndexAwareSearchWithForkJoinTask::new;

        // This constructor reference creates an
        // SearchWithForkJoinTask object.
        SearchWithForkJoinTaskFactory consRef =
            SearchWithForkJoinTask::new;

        System.out.println("The parallelism in the common fork-join pool is "
                           + ForkJoinPool.getCommonPoolParallelism());

        // Warm up the fork-join pool to account for any
        // instruction/data caching effects.
        warmUpForkJoinPool(indexAwareConsRef);

        // Run the substring tests.
        runTest(false, false, false, false, consRef);
        runTest(false, true, false, false, consRef);
        runTest(false, false, true, false, consRef);
        runTest(false, true, true, false, consRef);
        runTest(true, false, false, false, consRef);
        runTest(true, true, false, false, consRef);
        runTest(true, false, true, false, consRef);
        runTest(true, true, true, false, consRef);
        runTest(false, false, false, true, consRef);
        runTest(false, true, false, true, consRef);
        runTest(false, false, true, true, consRef);
        runTest(false, true, true, true, consRef);
        runTest(true, false, false, true, consRef);
        runTest(true, true, false, true, consRef);
        runTest(true, false, true, true, consRef);
        runTest(true, true, true, true, consRef);

        // Run the index-aware tests.
        runTest(false, false, false, false, indexAwareConsRef);
        runTest(false, true, false, false, indexAwareConsRef);
        runTest(false, false, true, false, indexAwareConsRef);
        runTest(false, true, true, false, indexAwareConsRef);
        runTest(true, false, false, false, indexAwareConsRef);
        runTest(true, true, false, false, indexAwareConsRef);
        runTest(true, false, true, false, indexAwareConsRef);
        runTest(true, true, true, false, indexAwareConsRef);
        runTest(false, false, false, true, indexAwareConsRef);
        runTest(false, true, false, true, indexAwareConsRef);
        runTest(false, false, true, true, indexAwareConsRef);
        runTest(false, true, true, true, indexAwareConsRef);
        runTest(true, false, false, true, indexAwareConsRef);
        runTest(true, true, false, true, indexAwareConsRef);
        runTest(true, false, true, true, indexAwareConsRef);
        runTest(true, true, true, true, indexAwareConsRef);

        // Print out the search results.
        printResults();

        System.out.println("Ending SearchStream");
    }

    /**
     * Run the test and print out the timing results.  The various @a
     * parallel* parameters indicates whether to run different parts
     * of the solution in parallel or not.
     */
    private static void runTest(boolean parallelSearching,
                                boolean parallelPhrases,
                                boolean parallelWorks,
                                boolean parallelInput,
                                SearchWithForkJoinTaskFactory consRef)
        throws IOException, URISyntaxException {
        // Record the start time.
        long startTime = System.nanoTime();

        // This list of CharSequence contains the complete works of
        // William Shakespeare, with one CharSequence for each work.
        List<CharSequence> inputList = TestDataFactory
            .getInput(sSHAKESPEARE_FOLDER,
                      parallelInput);

        // Create the appropriate type of SearchWithForkJoinTask.
        SearchWithForkJoinTask forkJoinTask =
            consRef.apply(inputList,
                          mPhrasesToFind,
                          parallelSearching,
                          parallelPhrases,
                          parallelWorks);

        // Record the configuration used to run this test.
        String testConfig = 
            ((forkJoinTask instanceof IndexAwareSearchWithForkJoinTask)
             ? "IndexAwareSearchWithForkJoinTask("
             : "SearchWithForkJoinTask(")
            + (parallelInput ? "parallel" : "sequential")
            + "Input|"
            + (parallelWorks ? "parallel" : "sequential")
            + "Work|"
            + (parallelPhrases ? "parallel" : "sequential")
            + "Phrases|"
            + (parallelSearching ? "parallel" : "sequential")
            + "Searching)";

        // Indicate which test configuration is running.
        System.out.println("Running test " + testConfig);

        // Use the common fork-join pool to search the input looking
        // for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
            .invoke(forkJoinTask);

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        // Store the results.
        storeResults(testConfig,
                     stopTime,
                     listOfListOfSearchResults);

        // Help the GC.
        //noinspection UnusedAssignment
        forkJoinTask = null;

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
    private static void storeResults(String testConfig,
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
                        + " phrase matches in "
                        + stopTime
                        + " milliseconds for "
                        + testConfig);

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


    /**
     * Warm up the fork-join pool to account for any instruction/data
     * caching effects.
     */
    private static void warmUpForkJoinPool(SearchWithForkJoinTaskFactory consRef)
        throws IOException, URISyntaxException {
        System.out.println("Warming up the fork-join pool");

        // This object is used to search a recursive directory
        // containing the complete works of William Shakespeare.
        List<CharSequence> inputList = TestDataFactory
            .getInput(sSHAKESPEARE_FOLDER, true);

        // Create the appropriate type of object.
        SearchWithForkJoinTask forkJoinTask =
            consRef.apply(inputList,
                          mPhrasesToFind,
                          true,
                          true,
                          true);

        @SuppressWarnings("unused")
        // Search the input looking for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
                        .invoke(forkJoinTask);

        // Help the GC.
        //noinspection UnusedAssignment
        forkJoinTask = null;

        // Run the garbage collector after each test.
        System.gc();
    }
}
