import search.SearchResult;
import search.SearchWithSpliterator;
import utils.TestDataFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * This example implements an "embarrassingly parallel" program that
 * uses Java 8 functional programming features (such as lambda
 * expressions, method references, functional interfaces,
 * sequential/parallel streams, a fork/join pool, and a spliterator)
 * to concurrently search for phrases in a string containing all the
 * works of Shakespeare.
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
     * A List of strings containing the complete works of Shakespeare.
     */
    private static List<String> mInput;

    /**
     * The list of phrases to find.
     */
    private static List<String> mPhrasesToFind;
        
    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Create a list of input strings from the works of
        // Shakespeare.
        mInput =
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     "#");

        // Get the list of phrases to find in the works of
        // Shakespeare.
        mPhrasesToFind = TestDataFactory
            .getPhraseList(sPHRASE_LIST_FILE);

        // Run the tests multiple times to account for any
        // instruction/data caching effects, as well as the time
        // needed to initialize the common fork/join pool.
        runTest(false);
        runTest(true);
        runTest(false);
        runTest(true);
        runTest(false);
        runTest(true);

        System.out.println("Ending SearchStream");
    }

    /**
     * Run the test and print out the timing results.  The @a parallel
     * parameter indicates whether to run the spliterator concurrently
     * or not.
     */
    private static void runTest(boolean parallel) {
        // Iterate through all the input strings.
        for (int iteration = 0;
             iteration < mInput.size();
             iteration++) {
            // Record the start time.
            long startTime = System.nanoTime();

            // Search the input looking for phrases that match.
            List<List<SearchResult>> listOfListOfSearchResults =
                new SearchWithSpliterator(mInput.get(iteration),
                                          mPhrasesToFind,
                                          parallel).processStream();

            // Record the stop time.
            long stopTime = (System.nanoTime() - startTime) / 1_000_000;

            // Print the number of times each phrase matched the input.
            System.out.println("SearchStream"
                               + (parallel ? "(parallel)" : "(sequential)")
                               + ": The search returned = "
                               // Count the number of matches.
                               + listOfListOfSearchResults.stream()
                                                          .mapToInt(List::size)
                                                          .sum()
                               + " phrase matches for input string "
                               + iteration + 1
                               + " in "
                               + stopTime
                               + " milliseconds");

            // Run the garbage collector after each test.
            System.gc();

            // Uncomment this to display all the results.
            displayResults(listOfListOfSearchResults);
        }
    }

    /**
     * Display all the search results.
     */
    private static void displayResults(List<List<SearchResult>> listOfListOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found.
        Map<String, List<SearchResult>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Flatten the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a Map.
            .collect(groupingBy(SearchResult::getPhrase));

        // Print out the results in the map, where each phrase is
        // first printed followed by a list of the indices where the
        // phrase appeared in the input.
        resultsMap.forEach((key, value)
                           -> System.out.println("Phrase \""
                                                 + key
                                                 + "\" matched at "
                                                 + value));
    }
}
