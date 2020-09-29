package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use Java parallel
 * streams to perform a concurrent search of each input string and
 * each phrase (from a list of phrases) within each input string.
 */
public class SearchWithParallelStreams
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreams(List<String> phrasesToFind,
                                     List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java stream to search for
     * phrases in the input data in parallel.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Get the list of input strings.
        List<CharSequence> inputList = getInput();

    	// Process the input strings via a parallel stream.
        return inputList
            // Concurrently process each string in the input list.
            .parallelStream()

            // Map each input string to list of SearchResults
            // containing the phrases found in the input.
            .map(this::processInput)

            // Terminate stream and return a list of lists of
            // SearchResults.
            .collect(toList());
    }
    
    /**
     * Concurrently search {@code input} for all occurrences of the
     * phrases to find.
     */
    private List<SearchResults> processInput(CharSequence input) {
        // Get the section title.
        String title = getTitle(input);

        // Skip over the title.
        CharSequence input = input.subSequence(title.length(),
                                               input.length());

        // Iterate through each phrase we're searching for and try to
        // find it in the inputData.
        List<SearchResults> results = mPhrasesToFind
            // Convert the list of phrases into a parallel stream.
            .parallelStream()
            
            // Find all indices where phrase matches the input data.
            .map(phrase -> searchForPhrase(phrase,
                                           input,
                                           title,
                                           false))
            
            // Only keep a result that has at least one match.
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
            
        // Return the results.
        return results;
    }
}

