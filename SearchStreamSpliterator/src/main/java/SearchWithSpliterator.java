import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This class searches for words in the works of Shakespeare.  It
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
     * The list of words to find.
     */
    private List<String> mWordsToFind;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private boolean mParallel;

    /**
     * Construtor initializes the fields.
     */
    public SearchWithSpliterator(String input,
                                 List<String> wordsToFind,
                                 boolean parallel) {
        mInput = input;
        mWordsToFind = wordsToFind;
        mParallel = parallel;
    }

    /**
     * Performs stream processing on the input.
     */
    public List<List<SearchResult>> processStream() {
        // Create a list of SearchResult objects that indicate which
        // words are found in the input string.
        return Stream
            // Convert the input string into a stream.
            .of(mInput)

            // Process each input string to find all occurrences of
            // the search words.
            .flatMap(this::processInput)

            // This terminal operation triggers aggregate operation
            // processing and returns a list of list of SearchResults.
            .collect(toList());
    }

    /**
     * This method searches the @a inputData for all occurrences of
     * the words to find.
     */
    private Stream<List<SearchResult>> processInput(String inputData) {
        // Find all occurrences of word in the input string.
        return mWordsToFind
            // Convert the list of words to find into a stream.
            .stream()

            // For each word create a list of SearchResults indicating
            // all places (if any) where the word matched the input.
            .map(word 
                 -> StreamSupport
                 // Create a stream of SearchResult objects that match
                 // the number of times a word appears in an input
                 // string.
                 .stream(new WordMatchSpliterator(inputData,
                                                  word),
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
