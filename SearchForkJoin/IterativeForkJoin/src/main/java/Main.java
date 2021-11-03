import search.SearchResults;
import search.SearchWithForkJoinTask;
import utils.Folder;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.groupingBy;
import static utils.StreamsUtils.PentaFunction;

/**
 * This example implements an "embarrassingly parallel" program that
 * uses the Java 7 fork-join framework in conjunction with Java 8
 * sequential and/or parallel streams to search for phrases in a
 * recursive directory folder containing the works of Shakespeare.
 * This example is (very) loosely based on the fork-join tutorial at
 * http://www.oracle.com/technetwork/articles/java/fork-join-422606.html,
 * but is much more powerful and interesting.
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
     * This is the main entry point into the program.
     */
    static public void main(String[] args)
        throws IOException, URISyntaxException {
        System.out.println("Starting SearchStream");

        // Parse the command-line arguments.
        Options.getInstance().parseArgs(args);

        // Get the list of phrases to find in the works of
        // Shakespeare.
        mPhrasesToFind = TestDataFactory
            .getPhraseList(sPHRASE_LIST_FILE);

        System.out.println("The parallelism in the common fork-join pool is "
                           + ForkJoinPool.getCommonPoolParallelism());

        // Warm up the fork-join pool to account for any
        // instruction/data caching effects.
        warmUpForkJoinPool();

        // Run the tests.
        runTest(false, false, false, false);
        runTest(false, false, true, false);
        runTest(false, false, false, true);
        runTest(false, false, true, true);
        runTest(false, true, false, false);
        runTest(false, true, true, false);
        runTest(false, true, false, true);
        runTest(false, true, true, true);
        runTest(true, false, false, false);
        runTest(true, false, true, false);
        runTest(true, false, false, true);
        runTest(true, false, true, true);
        runTest(true, true, false, false);
        runTest(true, true, true, false);
        runTest(true, true, false, true);
        runTest(true, true, true, true);

        // Print out the search results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending SearchStream");
    }

    /**
     * Warm up the fork-join pool to account for any instruction/data
     * caching effects.
     */
    private static void warmUpForkJoinPool() throws IOException, URISyntaxException {
        System.out.println("Warming up the fork-join pool");

        // This object is used to search a recursive directory
        // containing the complete works of William Shakespeare.
        Folder folder = TestDataFactory
            .getRootFolder(sSHAKESPEARE_FOLDER,
                           true);

        // Create the appropriate type of object.
        SearchWithForkJoinTask forkJoinTask =
            new SearchWithForkJoinTask(folder,
                                       mPhrasesToFind,
                                       true, 
                                       true,
                                       true,
                                       true);

        // Use the common fork-join pool to search the input looking
        // for phrases that match.
        List<List<SearchResults>> listOfListOfSearchResults =
            ForkJoinPool.commonPool()
            .invoke(forkJoinTask);

        // Help the GC.
        //noinspection UnusedAssignment
        forkJoinTask = null;

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Run the test and print out the timing results.  The various
     * {@code parallel*} parameters indicates whether to run different
     * parts of the solution in parallel or not.
     *
     * @param parallelSearching
     * @param parallelPhrases   True if phrases should be searched in parallel
     * @param parallelDocs      True if 
     * @param parallelWorks
     */
    private static void runTest(boolean parallelSearching,
                                boolean parallelPhrases,
                                boolean parallelDocs,
                                boolean parallelWorks)
            throws IOException, URISyntaxException {
        // Record the configuration used to run this test.
        String testConfig = 
            ("SearchWithForkJoinTask(")
            + (parallelWorks ? "parallel" : "sequential")
            + "Works|"
            + (parallelDocs ? "parallel" : "sequential")
            + "Docs|"
            + (parallelPhrases ? "parallel" : "sequential")
            + "Phrases|"
            + (parallelSearching ? "parallel" : "sequential")
            + "Searching)";

        // Indicate which test configuration is running.
        System.out.println("Running test " + testConfig);

        // This object is used to search a recursive directory
        // containing the complete works of William Shakespeare.
        Folder folder = TestDataFactory
            .getRootFolder(sSHAKESPEARE_FOLDER,
                           parallelWorks);

        // Create the appropriate type of object.
        final SearchWithForkJoinTask forkJoinTask =
            new SearchWithForkJoinTask(folder,
                                       mPhrasesToFind,
                                       parallelSearching,
                                       parallelPhrases,
                                       parallelDocs,
                                       parallelWorks);

        // Store the results.
        AtomicReference<List<List<SearchResults>>> results =
            new AtomicReference<>();

        RunTimer
            .timeRun(() -> {
                    results.set(ForkJoinPool
                        // Use the common fork-join pool to search the
                        // input looking for phrases that match.
                        .commonPool()
                        .invoke(forkJoinTask));
                },
            testConfig);

        System.out.println("The search returned = "
                           + results.get().stream()
                           .mapToInt(list
                                     -> list.stream().mapToInt(SearchResults::size).sum())
                           .sum()
                           + " phrase matches");

        // Print the matching titles.
        if (Options.getInstance().isVerbose())
            printTitles(results.get());

        // Run the garbage collector after each test.
        System.gc();
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
