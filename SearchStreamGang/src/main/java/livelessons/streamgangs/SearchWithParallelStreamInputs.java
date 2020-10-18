package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import javax.naming.directory.SearchResult;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use a Java Stream to
 * concurrently search each input string and then sequentially search
 * for each phrase (from a list of phrases) in the input string.
 */
public class SearchWithParallelStreamInputs
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamInputs(List<String> phrasesToFind,
                                          List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java stream to
     * concurrently search each input string for phrases to find.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
    	// Get the input.
        return getInput()
            // Concurrently process each string in the input list.
            .parallelStream()

            // Concurrently map each string to a stream containing the
            // phrases found in the input string.
            .map(this::processInput)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
    }

    /**
     * Search the {@code inputSeq} for all occurrences of the phrases to
     * find.
     */
    private List<SearchResults> processInput(CharSequence inputSeq) {
        // Get the section title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(),
                                                  inputSeq.length());

        // Sequentially iterate through each phrase we're searching
        // for and try to find it in the input.
        return mPhrasesToFind
            // Convert the list of phrases into a sequential stream.
            .stream()

            // Sequentially search for all places where the phrase
            // matches the input.
            .map(phrase -> 
                 searchForPhrase(phrase,
                                 input,
                                 title,
                                 false))

            // Only keep a result that has at least one match.
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate the stream and return a list of
            // SearchResults.
            .collect(toList());
    }
}

