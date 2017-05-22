import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This class searches for phrases in the works of Shakespeare.  It
 * demonstrates the use of Java 8 functional programming features,
 * such as lambda expressions, method references, functional
 * interfaces, sequential/parallel streams, a fork/join pool, and a
 * spliterator.
 */
public class SearchWithSpliterator {
    /**
     * The string to search.
     */
    private String mInput;

    /**
     * The list of phrases to find.
     */
    private List<String> mPhrasesToFind;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private boolean mParallel;

    /**
     * Construtor initializes the fields.
     */
    public SearchWithSpliterator(String input,
                                 List<String> phrasesToFind,
                                 boolean parallel) {
        mInput = input;
        mPhrasesToFind = phrasesToFind;
        mParallel = parallel;
    }

    /**
     * Performs stream processing on the input.
     */
    public List<List<SearchResult>> processStream() {
        // Create a list of SearchResult objects that indicate which
        // phrases are found in the input string.
        return Stream
            // Convert the input string into a stream.
            .of(mInput)

            // Process each input string to find all occurrences of
            // the search phrases.
            .flatMap(this::processInput)

            // This terminal operation triggers aggregate operation
            // processing and returns a list of list of SearchResults.
            .collect(toList());
    }

    /**
     * This method searches the @a inputData for all occurrences of
     * the phrases to find.
     */
    private Stream<List<SearchResult>> processInput(String inputData) {
        // Find all occurrences of phrase in the input string.
        return mPhrasesToFind
            // Convert the list of phrases to find into a stream.
            .stream()

            // For each phrase create a list of SearchResults indicating
            // all places (if any) where the phrase matched the input.
            .map(phrase 
                 -> StreamSupport
                 // Create a stream of SearchResult objects that match
                 // the number of times a phrase appears in an input
                 // string.
                 .stream(new PhraseMatchSpliterator(inputData,
                                                    phrase),
                         // Indicates whether to run the spliterator
                         // concurrently or not.
                         mParallel)

                 // This terminal operation triggers aggregate
                 // operation processing and returns a list of
                 // SearchResults.
                 .collect(toList()))

            // Filter out any list that has no SearchResults.
            .filter(list -> list.size() > 0);
    }
}
