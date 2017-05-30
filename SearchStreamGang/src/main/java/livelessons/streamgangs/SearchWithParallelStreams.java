package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use Java parallel
 * streams to concurrently search each input data string and
 * concurrently search for each phrase (from an array of phrases) within
 * each input data string.
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
     * Perform the processing, which uses a Java 8 Stream to
     * sequentially search for phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
         // Concurrently iterate through each phrase we're searching for
        // and try to find it in the input data.
        return mPhrasesToFind
            // Convert the array of phrases into a parallel stream.
            .parallelStream()
            
            // Concurrently search for all places where the phrase
            // matches the input data.
            .map(this::processPhrase)

            // Terminate stream and return a list of lists of
            // SearchResults.
            .collect(toList());
   }
    
    /**
     * Concurrently Search the inputData for all occurrences of the
     * phrases to find.
     */
    private List<SearchResults> processPhrase(String phrase) {
  	// Get the input.
        return getInput()
            // Concurrently process each String in the input list.
            .parallelStream()

            // Concurrently map each string to a stream containing the
            // phrases found in the input.
            .map(input -> {
                    // Get the section title.
                    String title = getTitle(input);

                    // Find all occurrences of phrase in the input string.
                    return searchForPhrase(phrase, 
                                           // Skip over the title.
                                           input.subSequence(title.length(), input.length()),
                                           title,
                                           false);
                })
            
            // Only keep a result that has at least one match.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
    }
}

