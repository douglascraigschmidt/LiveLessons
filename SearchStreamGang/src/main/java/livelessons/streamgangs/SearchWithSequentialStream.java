package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import javax.naming.directory.SearchResult;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use Java Streams to
 * sequentially search an input data string for each word in an array
 * of words.
 */
public class SearchWithSequentialStream
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithSequentialStream(List<String> wordsToFind,
                                      List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream to
     * sequentially search for words in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
    	// Get, process, and return the input data strings in a
    	// stream.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

            // Map each input string to list of SearchResults
            // containing the words found in the input.
            .map(this::processInput)

            // Terminate stream and return a list of lists of
            // SearchResults.
            .collect(toList());
    }
    
    /**
     * Sequentially search @a inputString for all occurrences of the
     * words to find.
     */
    private List<SearchResults> processInput(String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        List<SearchResults> results = mWordsToFind
            // Convert the array of words into a Stream.
            .stream()
            
            // Find all indices where word matches the input data.
            .map(word -> searchForWord(word,
                                       input,
                                       title,
                                       false))
            
            // Only keep a result that has at least one match.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
            
        // Return the results.
        return results;
    }
}

