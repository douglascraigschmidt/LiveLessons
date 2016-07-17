package streamgangs;

import static java.util.stream.Collectors.toList;

import java.util.List;

import utils.SearchResults;

/**
 * Customizes the SearchStreamGang framework to use Java Streams to
 * sequentially search input data for each word in an array of words.
 */
public class SearchWithSequentialStream
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithSequentialStream(List<String> wordsToFind,
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
    protected List<List<SearchResults>> processStream() {
    	// Get and process the input.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

            // Map each input string to list of SearchResults
            // containing the words found in the input.
            .map(this::processInput)

            // Terminate the stream.
            .collect(toList());
    }
    
    /**
     * Search the inputString for all occurrences of the words to find.
     */
    protected List<SearchResults> processInput (String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a Stream.
            .stream()
            
            // Search for all places where the word matches the input
            // data.
            .map(word -> searchForWord(word,
                                       input,
                                       title))
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)
            
            .collect(toList());
    }
}

