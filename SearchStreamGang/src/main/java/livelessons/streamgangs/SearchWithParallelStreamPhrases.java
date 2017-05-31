package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang superclass to use CompletableFutures
 * in conjunction with Java streams to asynchronously search the input
 * data for each phrase in an list of phrases.
 */
public class SearchWithParallelStreamPhrases
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamPhrases(List<String> phrasesToFind,
                                         List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for phrases in the input strings.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Concurrently iterate through each phrase we're searching for
        // and try to find it in the input strings.
        return mPhrasesToFind
            // Convert the list of phrases into a parallel stream.
            .parallelStream()
            
            // Concurrently search for all places where the phrase
            // matches the input strings.
            .map(this::processPhrase)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
   }
    
    /**
     * Sequentially search the list of input strings for all
     * occurrences of the @a phrase passed as a parameter.
     */
    private List<SearchResults> processPhrase(String phrase) {
     	// Get the input.
        return getInput()
            // Convert the list of input strings into a sequential
            // stream.
            .stream()

            // Map each string to a stream containing SearchResults if
            // the phrase is found in each input string.
            .map(inputSeq -> {
                // Get the section title.
                String title = getTitle(inputSeq);

                // Skip over the title.
                CharSequence input = inputSeq.subSequence(title.length(),
                                                          inputSeq.length());

                // Find all occurrences of phrase in the input string.
            	return searchForPhrase(phrase,
                                       input,
                                       title,
                                       false);
            })
            
            // Only keep a result that has at least one match.
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate stream and return a list of SearchResults.
            .collect(toList());
    }
}

