package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use a Java stream to
 * concurrently search each input data string and use a parallel
 * spliterator to search for each phrase (from an array of phrases) in
 * each input data string.
 */
public class SearchWithParallelSpliterator
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelSpliterator(List<String> phrasesToFind,
                                         List<List<CharSequence>> stringsToSearch) {
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
            .stream()

            // Concurrently map each string to a stream containing the
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
    private List<SearchResults> processInput(CharSequence inputSeq) {
        // Get the section title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(), 
                                                  inputSeq.length());

        // Find all occurrences of phrase in the input string.
        return mPhrasesToFind
            // Convert the list of phrases to find into a stream.
            .stream()

            // Find all indices where phrase matches the input data.
            .map(phrase -> searchForPhrase(phrase,
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

