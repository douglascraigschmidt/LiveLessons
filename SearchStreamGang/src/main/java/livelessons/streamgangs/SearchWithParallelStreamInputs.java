package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import javax.naming.directory.SearchResult;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use a Java Stream to
 * concurrently search each input data String and the sequentially
 * looking for each phrase (from an array of phrases) in the input data
 * String.
 */
public class SearchWithParallelStreamInputs
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamInputs(List<String> phrasesToFind,
                                          List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search each input string for phrases to find.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
    	// Get the input.
        return getInput()
            // Concurrently process each string in the input list.
            .parallelStream()

            // Concurrently map each string to a Stream containing the
            // phrases found in the input string.
            .map(this::processInput)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
    }

    /**
     * Search the @a inputString for all occurrences of the phrases to
     * find.
     */
    private List<SearchResults> processInput(String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        // Sequentially iterate through each phrase we're searching for
        // and try to find it in the inputData.
        return mPhrasesToFind
            // Convert the array of phrases into a sequential stream.
            .stream()

            // Sequentially search for all places where the phrase
            // matches the input data.
            .map(phrase -> 
                 searchForPhrase(phrase,
                                 input,
                                 title,
                                 false))

            // Only keep a result that has at least one match.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate the stream and return a list of
            // SearchResults.
            .collect(toList());
    }
}

