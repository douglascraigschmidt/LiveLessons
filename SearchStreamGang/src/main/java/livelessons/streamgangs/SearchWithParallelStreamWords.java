package livelessons.streamgangs;

import java.util.List;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use Java Streams to
 * concurrently search for all words in an array of input data.
 */
public class SearchWithParallelStreamWords
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreamWords(List<String> wordsToFind,
                                         String[][] stringsToSearch) {
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
            // Convert the array of words into a parallel stream.
            .parallelStream()
            
            // Concurrently search for all places where the word
            // matches the input data.
            .map(this::processWord)

            // Terminate the stream and return a list of lists of
            // SearchResults.
            .collect(toList());
   }
    
    /**
     * Sequentially search the input strings for all occurrences of
     * the word to find.
     */
    private List<SearchResults> processWord(String word) {
     	// Get the input.
        return getInput()
            // Convert the list of input strings into a sequential
            // stream.
            .stream()

            // Map each string to a stream containing the words found
            // in each input string.
            .map(inputString -> {
                // Get the section title.
                String title = getTitle(inputString);

                // Find all occurrences of word in the input string.
            	return searchForWord(word,
                                     // Skip over the title.
                                     inputString.substring(title.length()),
                                     title);
            })
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
    }
}

