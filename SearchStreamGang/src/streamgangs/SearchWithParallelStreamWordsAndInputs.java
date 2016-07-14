package streamgangs;

import static java.util.stream.Collectors.toList;

import java.util.List;

import utils.SearchResults;

/**
 * Customizes the SearchStreamGangCommon framework to use Java Streams
 * to concurrently search each input data string and concurrently
 * search for each word (from an array of words) within each input
 * data string.
 */
public class SearchWithParallelStreamWordsAndInputs
       extends SearchStreamGangSync {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamWordsAndInputs(List<String> wordsToFind,
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
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return mWordsToFind
            // Convert the array of words into a parallel stream.
            .parallelStream()
            
            // Search for all places where the word matches the input
            // data.
            .map(this::processWord)

            // Terminate the stream.
            .collect(toList());
   }
    
    /**
     * Concurrently Search the inputData for all occurrences of the
     * words to find.
     */
    protected List<SearchResults> processWord(String word) {
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
            .filter(result -> result.size() > 0)
            
            .collect(toList());
    }
}

