package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import livelessons.utils.SearchResults;
import livelessons.utils.WordMatchSpliterator;

import javax.naming.directory.SearchResult;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use a Java stream to
 * concurrently search each input data string and use a parallel
 * spliterator to search for each word (from an array of words) in
 * each input data string.
 */
public class SearchWithParallelSpliterator
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelSpliterator(List<String> wordsToFind,
                                         List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search each input string for words to find.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
    	// Get the input.
        return getInput()
            // Concurrently process each string in the input list.
            .parallelStream()

            // Concurrently map each string to a stream containing the
            // words found in the input string.
            .map(this::processInput)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
    }

    /**
     * Search the @a inputString for all occurrences of the words to
     * find.
     */
    private List<SearchResults> processInput(String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        // Find all occurrences of word in the input string.
        return mWordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // Find all indices where word matches the input data.
            .map(word -> searchForWord(word,
                                       input,
                                       title,
                                       true))

            // Only keep a result that has at least one match.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate the stream and trigger the processing.
            .collect(toList());
    }
}

