package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang superclass to use CompletableFutures
 * in conjunction with Java streams to asynchronously search the input
 * data for each word in an list of words.
 */
public class SearchWithParallelStreamWords
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamWords(List<String> wordsToFind,
                                         List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for words in the input strings.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Concurrently iterate through each word we're searching for
        // and try to find it in the input strings.
        return mWordsToFind
            // Convert the list of words into a parallel stream.
            .parallelStream()
            
            // Concurrently search for all places where the word
            // matches the input strings.
            .map(this::processWord)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
   }
    
    /**
     * Sequentially search the list of input strings for all
     * occurrences of the @a word passed as a parameter.
     */
    private List<SearchResults> processWord(String word) {
     	// Get the input.
        return getInput()
            // Convert the list of input strings into a sequential
            // stream.
            .stream()

            // Map each string to a stream containing SearchResults if
            // the word is found in each input string.
            .map(inputString -> {
                // Get the section title.
                String title = getTitle(inputString);

                // Find all occurrences of word in the input string.
            	return searchForWord(word,
                                     // Skip over the title.
                                     inputString.substring(title.length()),
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

