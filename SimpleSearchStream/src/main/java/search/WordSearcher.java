package search;

import utils.StreamUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static utils.StreamUtils.not;

/**
 * This class demonstrates the use of basic modern Java functional
 * programming features (such as lambda expressions and method
 * references) in conjunction with Java sequential streams and a
 * {@link Spliterator} to search for words in an input string.
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
     * Search the input for all occurrences of the {@code wordsToFind}
     * using the {@code map()} and {@code filter()} intermediate operators
     * and return the results (if any) as a {@link List} of {@link
     * SearchResults} objects.
     */
    public List<SearchResults> findWords(List<String> wordsToFind) {
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
            .toList();
    }

    /**
     * Search the input for all occurrences of the {@code wordsToFind}
     * using the {@code mapMulti()} intermediate operator and return
     * the results (if any) as a {@link List} of {@link
     * SearchResults} objects.
     */
    public List<SearchResults> findWordsEx(List<String> wordsToFind) {
        // Create and return a list SearchResults corresponding to the
        // index where each word occurs in the input string.
        return wordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // For each word to find create a stream of SearchResults
            // indicating the index (if any) where the word matched
            // the input.  Only update the consumer if there's a
            // match.
            .mapMulti(this::searchForWordIf)

            // This terminal operation triggers intermediate operation
            // processing and collects the SearchResults into a list.
            .toList();
    }

    /**
     * Looks for all instances of {@code word} in {@code inputData}
     * and return a {@link List} of all the {@code SearchResult}
     * objects (if any).
     */
    private SearchResults searchForWord(String word) {
        // Create/return SearchResults to keep track of relevant info.
        return new SearchResults
            (Thread.currentThread().threadId(),
             1,
             word,
             "",

             // Create a sequential stream of Result objects that
             // record where the word matched the input (if it did).
             StreamSupport
             // Use a WordMatchSpliterator to add the indices of all
             // places in the input where word matches.
             .stream(new WordMatchSpliterator(mInput, word),
                     false)

             // This terminal operation triggers aggregate operation
             // processing and returns a list of Results.
             .toList());
    }

    /**
     * Calls {@code searchForWord} with the {@code word} parameter and
     * updates the {@link Consumer} if there's a match.
     *
     * @param word The word to search for
     * @param consumer The {@link Consumer} to update if there's a match
     */
    private void searchForWordIf(String word,
                                 Consumer<SearchResults> consumer) {
        // Create/return SearchResults to keep track of
        // matching words.
        var result = searchForWord(word);

        // Update the consumer if there's a match.
        if (!result.isEmpty())
            consumer.accept(result);
    }

    /**
     * Print a word and its list of indices to the output.
     */
    private void printResult
        (String word,
         List<SearchResults.Result> results) {
        // Print the word followed by the list of search results.
        System.out.print("Word \""
                         + word
                         + "\" appeared at indices ");
        SearchResults.printResults(results);
        System.out.println(" with max index of "
                           + computeMax(results));
    }

    /**
     * Compute the max index in the list of search results.  This
     * implementation works properly even if the {@code resultsList}
     * is not sorted.
     */
    private int computeMax
        (List<SearchResults.Result> resultsList) {
        return resultsList
            // Convert to a stream.
            .stream()

            // Transform the stream of results into a stream of
            // primitive int indices.
            .mapToInt(SearchResults.Result::getIndex)

            // Determine the max value in the stream.
            .max()

            // Convert the optional to its primitive int value or 0.
            .orElse(0);
    }

    /**
     * Print the results of the word search.
     */
    public void printResults
        (List<SearchResults> listOfSearchResults) {
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
                                toDownstreamCollector()))

            // Print out the results in the map, where each word is
            // first printed followed by a list of the indices where
            // the word appeared in the input.
            .forEach(this::printResult);
    }

    /*
     * This factory method creates a downstream collector that
     * merges results lists together.
     */
    private static Collector<SearchResults,
                              List<SearchResults.Result>,
                              List<SearchResults.Result>> 
        toDownstreamCollector() {
        // Use the Collector.of() factory method to create a
        // collector.
        return Collector.of
            // Make a mutable results list container from an array list.
            (ArrayList::new,
             // Accumulate elements from a SearchResults object into the results list.
             (resultsList, searchResults) ->
             resultsList.addAll(searchResults.getResultList()),
             // Combine the two results lists.
             StreamUtils::concat);
    }

    /**
     * Print a "prefix slice" of the {@code listOfSearchResults}
     * starting at the beginning of the list and continuing upto (but
     * not including) the given {@code word}.
     */
    public void printPrefixSlice(String word,
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
                                toDownstreamCollector()))

            // Get the EntrySet for this map.
            .entrySet()
            
            // Convert EntrySet into a stream.
            .stream()

            // Slice the stream to consist of the elements of this
            // stream up to (but not including) the word parameter.
            .takeWhile(entry -> notEqual(entry, word))

            // Print out the matching results in the stream, where
            // each word is first printed followed by a list of the
            // indices where the word appeared in the input.
            .forEach(entry ->
                     printResult(entry.getKey(),
                                 entry.getValue()));
    }

    /**
     * Print a "suffix slice" of the {@code listOfSearchResults}
     * starting at the given {@code word} and continuing to the end of
     * the list.
     */
    public void printSuffixSlice(String word,
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
                                toDownstreamCollector()))

            // Get the EntrySet for this map.
            .entrySet()
            
            // Convert EntrySet into a stream.
            .stream()

            // Slice the stream to consist of the elements of this
            // stream from the first match of the word parameter to
            // the end of the list.
            .dropWhile(entry -> notEqual(entry, word))

            // Print out the matching results in the stream, where
            // each word is first printed followed by a list of the
            // indices where the word appeared in the input.
            .forEach(entry ->
                     printResult(entry.getKey(),
                                 entry.getValue()));
    }

    /**
     * Return true if {@code entry.getKey()} != to {@code word}, else
     * false.
     */
    private boolean notEqual
        (Map.Entry<String, List<SearchResults.Result>> entry,
         String word) {
        // If entry.getKey() != to word return true, otherwise return
        // false.
        return !entry.getKey().equals(word);
    }
}

