package streamgangs;

import static java.util.stream.Collectors.toList;

import java.util.List;

import utils.SearchResults;

/**
 * Customizes the SearchStreamGangCommon framework to use a Java
 * Stream to concurrently search each input data String and the
 * sequentially looking for each word (from an array of words) in the
 * input data String.
 */
public class SearchWithParallelStreamInputs
       extends SearchStreamGangSync {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamInputs(List<String> wordsToFind,
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
    protected List<List<SearchResults>> processStream() {
    	// Get the input.
        return getInput()
            // Concurrently process each String in the input list.
            .parallelStream()

            // Map each String to a Stream containing the words found
            // in the input.
            .map(this::processInput)

            // Terminate the stream.
            .collect(toList());
    }

    /**
     * Search the inputData for all occurrences of the words to find.
     */
    protected List<SearchResults> processInput (String inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a parallel stream.
            .parallelStream()

            // Sequentially search for all places where the word
            // matches the input data.
            .map(word -> 
                 searchForWord(word,
                               input,
                               title))
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)
            
            .collect(toList());
    }
}

