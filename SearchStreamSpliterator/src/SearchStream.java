import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This example implements an "embarrassingly parallel" application
 * that concurrently searches for words in a list of strings.  It
 * demonstrates the use of Java 8 functional programming features,
 * such as lambda expressions, method references, functional
 * interfaces, sequential/parallel streams, a fork/join pool, and a
 * spliterator.
 */
public class SearchStream {
    /**
     * The list of words to find.
     */
    private static final List<String> mWordsToFind =
        Arrays.asList("do", "re", "mi", "fa", "so", "la", "ti", "do");
        
    /**
     * The List of strings to search.
     */
    private static final List<String> mInput =
        Arrays.asList("xreo", "xfao", "xmiomio", "xlao", "xtiotio", "xsoosoo", "xdoo", "xdoodoo",
                      "yreo", "yfao", "ymiomio", "ylao", "ytiotio", "ysoosoo", "ydoo", "ydoodoo",
                      "zreo", "zfao", "zmiomio", "zlao", "ztiotio", "zsoosoo", "zdoo", "zdoodoo");
    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) {
        System.out.println("Starting SearchStream");

        // Create a list of SearchResult objects that indicate which
        // words are found in the input strings.
        List<SearchResult> listSearchResults = mInput
            // Convert the list of input strings into a stream.
            .stream()

            // Process each input string to create a list of
            // SearchResults.
            .map(SearchStream::processInput)

            // Flatten the list of lists into a single list.
            .flatMap(List::stream)

            // This terminal operation triggers aggregate operation
            // processing and returns a list of SearchResults.
            .collect(toList());

        // Sort the lists by thread id.
        listSearchResults.sort(Comparator.comparingLong(sr 
                                                        -> sr.mThreadId));

        // Print all the SearchResult objects.
        listSearchResults
            .forEach(searchResults
                     -> 
                     // print each SearchResult.
                     System.out.println(" "
                                        + searchResults.mWord
                                        + " was found at offset "
                                        + searchResults.mIndex
                                        + " in string "
                                        + searchResults.mInputData
                                        + " in thread "
                                        + searchResults.mThreadId));

        System.out.println("Ending SearchStream");
    }

    /**
     * This method searches the @a inputData for all occurrences
     * of the words to find.
     */
    private static List<SearchResult> processInput(String inputData) {
        return mWordsToFind
            // Convert the list of words to find into a parallel
            // stream.
            .parallelStream()

            // Return a single stream containing SearchResult objects
            // that match each word (if any).
            .flatMap(word
                     ->
                     // Create a parallel stream containing a
                     // SearchResult indicating all places (if any)
                     // where the word matched the input.
                     StreamSupport.stream(new WordMatcherSpliterator
                                          (new WordMatcher(word,
                                                           inputData)),
                                          true))

            // Trigger processing and return a list of SearchResult
            // objects (if any).
            .collect(toList());
    }
}

