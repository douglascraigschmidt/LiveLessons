import search.SearchResult;
import search.SearchWithSpliterator;
import utils.TestDataFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * The string containing the complete works of Shakespeare.
     */
    private static String mInput;

    /**
     * The list of phrases to find.
     */
    private static List<String> mPhrasesToFind;
        
    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Create a single input string from the works of Shakespeare.
        mInput =
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     // Split using a character that's
                                     // not in the file.
                                     "#").get(0);

        // Get the list of phrases to find in the works of Shakespeare.
        mPhrasesToFind = TestDataFactory
            .getPhraseList(sPHRASE_LIST_FILE)
            .stream()
            // Remove any empty phrases in the list.
            .filter(phrase -> phrase.length() > 0)
            .collect(toList());

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
        // Record the start time.
        long startTime = System.nanoTime();

        // Search the input looking for phrases that match.
        List<List<SearchResult>> listOfListOfSearchResults =
            new SearchWithSpliterator(mInput,
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
                           + " phrase matches for the input in "
                           + stopTime
                           + " milliseconds");

        // Help the garbage collector.
        System.gc();

        // Uncomment this to display all the results.
        // displayResults(listOfListOfSearchResults);
    }

    /**
     * Display all the search results.
     */
    private static void displayResults(List<List<SearchResult>> listOfListOfSearchResults) {
        // Transform the list of lists of results to a simple list of
        // results.
        List<SearchResult> listOfSearchResults = listOfListOfSearchResults
            .stream()
            .flatMap(List::stream)
            .collect(toList());

        // Sort the list by the thread id.
        listOfSearchResults.sort(Comparator.comparing(sr
                                                      -> sr.mThreadId));

        // Display all the SearchResult objects.
        listOfSearchResults
            .forEach(searchResults
                     -> 
                     // print each SearchResult.
                     System.out.println(" "
                                        + searchResults.mPhrase
                                        + " was found at offset "
                                        + searchResults.mIndex
                                        // + " in string "
                                        // + searchResults.mInputData
                                        + " in thread "
                                        + searchResults.mThreadId));
    }
}

