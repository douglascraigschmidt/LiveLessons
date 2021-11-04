import search.IndexAwareSearchWithForkJoinTask;
import search.SearchResults;
import search.SearchWithForkJoinTask;
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
 * uses a modern Java fork-join pool and a "divide and conquer"
 * strategy (i.e., splitting various folders, phrases, and strings in
 * half) to search for phrases in a recursive directory folder
 * containing all the works of Shakespeare.  All parallel processing
 * in this program only uses "classic" Java 7 features (i.e., no Java
 * parallel streams) to demonstrate "raw" fork-join pool programming.
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
        SearchWithForkJoinTaskFactory constructorRef =
            SearchWithForkJoinTask::new;

        System.out.println("The parallelism in the common fork-join pool is "
                           + ForkJoinPool.getCommonPoolParallelism());

        // Warm up the fork-join pool to account for any
        // instruction/data caching effects.
        warmUpForkJoinPool(indexAwareConsRef);

        // Run the substring tests.
        runTest(false, false, false, false, constructorRef);
        runTest(false, true, false, false, constructorRef);
        runTest(false, false, true, false, constructorRef);
        runTest(false, true, true, false, constructorRef);
        runTest(true, false, false, false, constructorRef);
        runTest(true, true, false, false, constructorRef);
        runTest(true, false, true, false, constructorRef);
        runTest(true, true, true, false, constructorRef);
        runTest(false, false, false, true, constructorRef);
        runTest(false, true, false, true, constructorRef);
        runTest(false, false, true, true, constructorRef);
        runTest(false, true, true, true, constructorRef);
        runTest(true, false, false, true, constructorRef);
        runTest(true, true, false, true, constructorRef);
        runTest(true, false, true, true, constructorRef);
        runTest(true, true, true, true, constructorRef);

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
        System.out.println(RunTimer.getTimingResults());

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
                                SearchWithForkJoinTaskFactory constructorRef)
        throws IOException, URISyntaxException {
        // This list of CharSequence contains the complete works of
        // William Shakespeare, with one CharSequence for each work.
        List<CharSequence> inputList = TestDataFactory
            .getInput(sSHAKESPEARE_FOLDER,
                      parallelInput);

        // Create the appropriate type of SearchWithForkJoinTask.
        SearchWithForkJoinTask forkJoinTask =
            constructorRef.apply(inputList,
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

        // Store the results.
        AtomicReference<List<List<SearchResults>>> results =
            new AtomicReference<>();

        RunTimer
            .timeRun(() -> {
                    results
                        .set(ForkJoinPool
                             // Use the common fork-join pool to
                             // search input for phrases that match.
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


    /**
     * Warm up the fork-join pool to account for any instruction/data
     * caching effects.
     */
    private static void warmUpForkJoinPool(SearchWithForkJoinTaskFactory constructorRef)
        throws IOException, URISyntaxException {
        System.out.println("Warming up the fork-join pool");

        // This object is used to search a recursive directory
        // containing the complete works of William Shakespeare.
        List<CharSequence> inputList = TestDataFactory
            .getInput(sSHAKESPEARE_FOLDER, true);

        // Create the appropriate type of object.
        SearchWithForkJoinTask forkJoinTask = constructorRef
            .apply(inputList,
                   mPhrasesToFind,
                   true,
                   true,
                   true);

        ForkJoinPool
            // Use the common fork-join pool to search input for
            // phrases that match.
            .commonPool()
            .invoke(forkJoinTask);

        // Run the garbage collector after each test.
        System.gc();
    }
}
