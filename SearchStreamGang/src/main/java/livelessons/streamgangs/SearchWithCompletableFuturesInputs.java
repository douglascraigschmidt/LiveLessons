package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use CompletableFutures
 * in conjunction with Java streams to asynchronously search the input
 * data for each word in an list of words.
 */
public class SearchWithCompletableFuturesInputs
    extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithCompletableFuturesInputs(List<String> wordsToFind,
                                              List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream in
     * conjunction with CompletableFutures to asynchronously search for
     * words in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Convert the input strings into a list of
        // CompletableFutures.
        List<CompletableFuture<List<SearchResults>>> listOfFutures = getInput()
            // Create a sequential stream of input strings.
            .stream()

            // Map each input string to a CompletableFuture to a list
            // of SearchResults.
            .map(this::processInputAsync)
            
            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(toList());

        // Convert all the completed CompletableFutures in the
        // listOfFutures into a list of lists of SearchResults.
        return StreamsUtils.joinAll(listOfFutures)
                           // join() blocks the calling thread until
                           // all the futures have been completed.
                           .join();
    }

    /**
     * Asynchronously search @a inputString for all occurrences of the
     * words to find.
     */
    private CompletableFuture<List<SearchResults>> processInputAsync(String inputString) {
        // Store the title.
        String title = getTitle(inputString);

        // Skip over the title.
        String inputData = inputString.substring(title.length());

        // Convert the list of words into a list of CompletableFutures
        // to SearchResults.
        List<CompletableFuture<SearchResults>> listOfFutures = mWordsToFind
            // Create a sequential stream of words.
            .stream()

            // Asynchronously find each word in the input data and
            // return a CompletableFuture<SearchResults>.
            .map(word ->
                 CompletableFuture.supplyAsync(()
                                               -> searchForWord(word,
                                                                inputData,
                                                                title)))

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(toList());
                    
        // Return a CompletableFuture to a list of SearchResults that
        // will be complete when all the CompletableFutures in the
        // listOfFutures have completed.
        return StreamsUtils.joinAll(listOfFutures);
    }
}

