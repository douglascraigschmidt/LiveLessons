import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Customizes the SearchStreamGangCommon framework to use a sequential
 * Java Stream to search the input for an array of words to find. It
 * only runs for a single iteration cycle.
 */
public class SearchWithSequentialStream
             extends SearchStreamGangCommon {
    /**
     * Constructor initializes the super class.
     */
    SearchWithSequentialStream(String[] wordsToFind,
                               String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the ImageStream processing, which uses a Java 8 stream
     * to download, process, and store images concurrently.
     */
    @Override
    protected void processStream() {
    	// Get the input.
        List<String> inputStrings = getInput();

        List<SearchResults> collect = inputStrings
            // Sequentially process each String in the input list.
            .stream()

            // Map each String to a Stream containing the words found
            // in the input.
            .flatMap(this::processInput)

            // Terminate the stream.
            .collect(Collectors.toList());

        // Print the results.
        collect.stream().forEach(SearchResults::print);
  
        System.out.println(TAG 
                           + ": The search returned "
                           + collect.stream().mapToInt(SearchResults::size).sum()
                           + " word matches for " 
                           + inputStrings.size() 
                           + " input strings");
    }
}

