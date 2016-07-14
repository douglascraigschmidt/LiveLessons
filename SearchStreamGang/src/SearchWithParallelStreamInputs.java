import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Customizes the SearchStreamGangCommon framework to use a Java
 * Stream to concurrently search each input data String and the
 * sequentially looking for each word (from an array of words) in the
 * input data String.
 */
public class SearchWithParallelStreamInputs
             extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    SearchWithParallelStreamInputs(List<String> wordsToFind,
                                   String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for words in the input data.
     */
    @Override
    protected List<SearchResults> processStream() {
    	// Get the input.
        return getInput()
            // Concurrently process each String in the input list.
            .parallelStream()

            // Map each String to a Stream containing the words found
            // in the input.
            .flatMap(this::processInput)

            // Terminate the stream.
            .collect(Collectors.toList());
    }

    /**
     * Search the inputData for all occurrences of the words to find.
     */
    protected Stream<SearchResults> processInput (String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a parallel stream.
            .parallelStream()

            // Sequentially search for all places where the word
            // matches the input data.
            .map(word -> 
                 searchForWord(word,
                               // Skip over the title.
                               inputString.substring(title.length()),
                               title))
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0);
    }
}

