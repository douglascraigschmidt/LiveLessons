package search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

/**
 * This class demonstrates the use of basic Java 8 functional
 * programming features (such as lambda expressions and method
 * references) in conjunction with Java 8 sequential streams to search
 * for words in an input string.
 */
public class SearchInputForWords {
    /**
     * Input string to search.
     */
    private final String mInput;

    /**
     * Constructor initializes the field.
     */
    public SearchInputForWords(String input) {
        mInput = input;
    }

    /**
     * Searches the input for all occurrences of the @ wordsToFind and
     * prints them.
     */
    public void findAndPrintWords(List<String> wordsToFind) {
        // Create a list of lists of SearchResults corresponding to
        // the index where each word occurs in the input string.
        List<List<SearchResult>> results = wordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // For each word create a list of SearchResults indicating
            // all places (if any) where the word matched the input.
            .map(this::searchForWord)
            
            // Only consider lists that have SearchResults.
            .filter(((Predicate<List>) List::isEmpty).negate())
            
            // This terminal operation triggers intermediate operation
            // processing and collects results into a list.
            .collect(toList());

        // Print the results;
        printResults(results);
    }

    /**
     * Search for all instances of @code word in the input and return
     * a list of all the @code SearchResults (if any).
     */
    private List<SearchResult> searchForWord(String word) {
        // Create a list to store the results.
        List<SearchResult> results = 
            new ArrayList<>();

        // Check to see how many times (if any) the word appears in
        // the input.
        for (int index = mInput.indexOf(word, 0);
             index != -1;
             index = mInput.indexOf(word,
                                    index + word.length())) 
            // Each time a match is found it's added to the list of
            // search results.
            results.add(new SearchResult(mInput, word, index));

        return results;
    }

    /**
     * Print the results of the word search.
     */
    private void printResults(List<List<SearchResult>> listOfListOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found.
        Map<String, List<Integer>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Convert the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a Map.
            .collect(groupingBy(SearchResult::getWord, 
                                mapping(SearchResult::getIndex, toList())));

        // Print out the results in the map, where each word is first
        // printed followed by a list of the indices where the word
        // appeared in the input.
        resultsMap.forEach((key, value)
                           -> System.out.println("Word \""
                                                 + key
                                                 + "\" matched at index "
                                                 + value));
    }
}
