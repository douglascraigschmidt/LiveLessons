package search;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static utils.StreamsUtils.not;

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
     * return the results (if any) as a list of SearchResults.
     */
    public List<SearchResults> findWords(List <String> wordsToFind) {
        // Create and return a list SearchResults corresponding to the
        // index where each word occurs in the input string.
        return wordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // For each word to find create a stream of SearchResults
            // indicating the index (if any) where the word matched
            // the input.
            .map(this::searchForWord)
            
            // Filter out any SearchResults that are empty.
            .filter(not(SearchResults::isEmpty))
            
            // This terminal operation triggers intermediate operation
            // processing and collects the SearchResults into a list.
            .collect(toList());
    }

    /**
     * Looks for all instances of @code word in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    private SearchResults searchForWord(String word) {
    	// Create/return SearchResults to keep track of relevant info.
        return new SearchResults
            (Thread.currentThread().getId(),
             1,
             word,
             "",

             // Use a WordMatchSpliterator to add the indices of all
             // places in the input where word matches.
             StreamSupport
                 // Create a sequential stream of Result objects that
                 // record where the word matched the input (if it did).
                 .stream(new WordMatchSpliterator(mInput, word),
                         false)

             // This terminal operation triggers aggregate operation
             // processing and returns a list of Results.
             .collect(toList()));
    }

    /**
     * Print a word and its list of indices to the output.
     */
    private void printResult(String word,
                             List<SearchResults> results) {
        // Print the word followed by the list of search results.
        System.out.print("Word \""
                         + word
                         + "\" appeared at indices ");
        results.forEach(SearchResults::print);
    }

    /**
     * Print the results of the word search.
     */
    public void printResults(List<SearchResults> listOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found and then print the
        // contents of that map.
        listOfSearchResults
            // Convert the list into a stream.
            .stream()

            // Collect the SearchResults into a LinkedHashMap, which
            // preserves the insertion order.
            .collect(groupingBy(SearchResults::getWord,
                                LinkedHashMap::new,
                                toList()))

            // Print out the results in the map, where each word is
            // first printed followed by a list of the indices where
            // the word appeared in the input.
            .forEach(this::printResult);
    }

    /**
     * Print a slice of the {@code listOfSearchResults} starting at a particular {@code word}.
     */
    public void printSlice(String word,
                           List<SearchResults> listOfSearchResults) {
        // Create a map that associates words found in the input with
        // the indices where they were found and print out the results
        // in the map, where each word is first printed followed by a
        // list of the indices where the word appeared in the input.
        listOfSearchResults
            // Convert the list into a stream.
            .stream()

            // Collect the SearchResults into a LinkedHashMap, which
            // preserves the insertion order.
            .collect(groupingBy(SearchResults::getWord,
                                LinkedHashMap::new,
                                toList()))

            // Get the EntrySet for this map.
            .entrySet()
            
            // Convert EntrySet into a stream.
            .stream()

            // Slice the stream to consist of the remaining elements
            // of this stream after dropping the subset of elements
            // that don't match the word parameter.
            .dropWhile(entry -> notEqual(entry, word))

            // Print out the matching results in the stream, where
            // each word is first printed followed by a list of the
            // indices where the word appeared in the input.
            .forEach(entry -> printResult(entry.getKey(), entry.getValue()));
    }

    /**
     * Return true if {@code entry.getKey()} != to {@code word}, else false.
     */
    private boolean notEqual(Map.Entry<String, List<SearchResults>> entry, String word) {
        // If entry.getKey() != to word return true, otherwise return
        // false.
        return !entry.getKey().equals(word);
    }
}

