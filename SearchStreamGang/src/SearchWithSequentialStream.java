import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Customizes the SearchStreamGang framework to use Java Streams to
 * sequentially search input data for each word in an array of words.
 */
public class SearchWithSequentialStream
             extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    SearchWithSequentialStream(List<String> wordsToFind,
                               String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * sequentially search for words in the input data.
     */
    @Override
    protected List<SearchResults> processStream() {
    	// Get the input.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

            // Map each String to a Stream containing the words found
            // in the input.
            .flatMap(this::processInput)

            // Terminate the stream.
            .collect(Collectors.toList());
    }
    
    /**
     * Search the inputData for all occurrences of the words to find.
     */
    protected Stream<SearchResults> processInput (String inputData) {
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a Stream.
            .stream()
            
            // Search for all places where the word matches the input
            // data.
            .map(word -> searchForWord(word, inputData, ""))
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0);
    }
}

