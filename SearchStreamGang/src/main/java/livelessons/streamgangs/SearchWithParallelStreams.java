package livelessons.streamgangs;

import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use Java parallel
 * streams to concurrently search each input data string and
 * concurrently search for each word (from an array of words) within
 * each input data string.
 */
public class SearchWithParallelStreams
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithParallelStreams(List<String> wordsToFind,
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
         // Concurrently iterate through each word we're searching for
        // and try to find it in the input data.
        return mWordsToFind
            // Convert the array of words into a parallel stream.
            .parallelStream()
            
            // Concurrently search for all places where the word
            // matches the input data.
            .map(this::processWord)

            // Terminate stream and return a list of lists of
            // SearchResults.
            .collect(toList());
   }
    
    /**
     * Concurrently Search the inputData for all occurrences of the
     * words to find.
     */
    private List<SearchResults> processWord(String word) {
  	// Get the input.
        return getInput()
            // Concurrently process each String in the input list.
            .parallelStream()

            // Concurrently map each string to a stream containing the
            // words found in the input.
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
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
    }
}

