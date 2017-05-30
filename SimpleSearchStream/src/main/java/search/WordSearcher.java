package search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

/**
 * This class demonstrates the use of basic Java 8 functional
 * programming features (such as lambda expressions and method
 * references) in conjunction with Java 8 sequential streams and a
 * spliterator to search for words in an input string.
 */
public class WordSearcher {
    /**
     * Input string to search.
     */
    private final String mInput;

    /**
     * Constructor initializes the field.
     */
    public WordSearcher(String input) {
        mInput = input;
    }

    /**
     * Search the input for all occurrences of the @ wordsToFind and
     * print the results (if any).
     */
    public void findAndPrintWords(List <String> wordsToFind) {
        // Create a list SearchResults corresponding to the index
        // where each word occurs in the input string.
        List<SearchResults> results = wordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // For each word to find create a list of SearchResults
            // indicating the index (if any) where the word matched
            // the input.
            .map(this::searchForWord)
            
            // Filter out any SearchResults that are empty.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            
            // This terminal operation triggers intermediate operation
            // processing and collects the SearchResults into a list.
            .collect(toList());

        // Print the results;
        printResults(results);
    }

    /**
     * Looks for all instances of @code phrase in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    private SearchResults searchForWord(String word) {
        List<SearchResults.Result> resultList =
            // Use a WordMatchSpliterator to add the indices of all
            // places in the input where phrase matches.
            StreamSupport
            // Create a sequential stream of SearchResults.Result that
            // record where the word matched the input (if it did).
            .stream(new WordMatchSpliterator(mInput, word),
                    false)
                    
            // This terminal operation triggers aggregate operation
            // processing and returns a list of Results.
            .collect(toList());

    	// Create/return SearchResults to keep track of relevant info.
        return new SearchResults(Thread.currentThread().getId(),
                                 1,
                                 word,
                                 "",
                                 resultList);
    }

    /**
     * Print the results of the word search.
     */
    private void printResults(List<SearchResults> listOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found.
        Map<String, List<SearchResults>> resultsMap = listOfSearchResults
            // Convert the list into a stream.
            .stream()

            // Collect the SearchResults into a Map.
            .collect(groupingBy(SearchResults::getWord));

        // Print out the results in the map, where each word is first
        // printed followed by a list of the indices where the word
        // appeared in the input.
        resultsMap.forEach((key, value)
                           -> {
                               System.out.print("Word \""
                                                + key
                                                + "\" appeared at indices ");
                               value.forEach(SearchResults::print);
                           });
    }
}
