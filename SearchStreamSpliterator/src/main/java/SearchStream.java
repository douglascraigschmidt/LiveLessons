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
 * This example implements an "embarrassingly parallel" application
 * that concurrently searches for words in string containing all the
 * works of Shakespeare.
 */
public class SearchStream {
    /**
     * Input files.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "shakespeare.txt";
    private static final String sWORD_LIST_FILE =
        "wordList.txt";

    /**
     * The string to search.
     */
    private static String mInput;

    /**
     * The list of words to find.
     */
    private static List<String> mWordsToFind;
        
    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Create an input string from the works of Shakespeare.
        mInput = TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE);

        // Get the list of words to find in the works of Shakespeare.
        mWordsToFind = TestDataFactory
            .getWordsList(sWORD_LIST_FILE)
            .stream()
            // Remove any empty words in the list.
            .filter(word -> word.length() > 0)
            .collect(toList());

        // Run the tests multiple times to account for caching
        // effects.
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

        // Search the input looking for words that match.
        List<List<SearchResult>> listOfListOfSearchResults =
            new SearchWithSpliterator(mInput,
                                      mWordsToFind,
                                      parallel).processStream();

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        // Print the number of times each word matched the input.
        System.out.println("SearchStream"
                           + (parallel ? "(parallel)" : "(sequential)")
                           + ": The search returned = "
                           + listOfListOfSearchResults.stream()
                                                      .mapToInt(List::size)
                                                      .sum()
                           + " word matches for the input in "
                           + stopTime
                           + " milliseconds");

        // Help the garbage collector.
        System.gc();
        
        /*
          List<SearchResult> listOfSearchResults = listOfListOfSearchResults
          .stream()
          .flatMap(List::stream)
          .collect(toList());

          // Sort the list.
          listOfSearchResults.sort(Comparator.comparing(sr
          -> sr.mThreadId));

          // Print all the SearchResult objects.
          listOfSearchResults
          .forEach(searchResults
          -> 
          // print each SearchResult.
          System.out.println(" "
          + searchResults.mWord
          + " was found at offset "
          + searchResults.mIndex
          // + " in string "
          // + searchResults.mInputData
          + " in thread "
          + searchResults.mThreadId));
        */
    }
}

