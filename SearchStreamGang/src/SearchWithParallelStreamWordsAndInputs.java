import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Customizes the SearchStreamGangCommon framework to use Java Streams
 * to concurrently search each input data string and concurrently
 * search for each word (from an array of words) within each input
 * data string.
 */
public class SearchWithParallelStreamWordsAndInputs
             extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    SearchWithParallelStreamWordsAndInputs(List<String> wordsToFind,
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
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a parallel stream.
            .parallelStream()
            
            // Search for all places where the word matches the input
            // data.
            .flatMap(this::processWord)

            // Terminate the stream.
            .collect(Collectors.toList());
   }
    
    /**
     * Concurrently Search the inputData for all occurrences of the
     * words to find.
     */
    protected Stream<SearchResults> processWord(String word) {
  	// Get the input.
        return getInput()
            // Concurrently process each String in the input list.
            .parallelStream()

            // Map each String to a Stream containing the words found
            // in the input.
            .map(inputString -> {
                    // Get the section title.
                    String title = getTitle(inputString);

                    return searchForWord(word, 
                                         // Skip over the title.
                                         inputString.substring(title.length()),
                                         title);
                        })
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0);
    }
}

