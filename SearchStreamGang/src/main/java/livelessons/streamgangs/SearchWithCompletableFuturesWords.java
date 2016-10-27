package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use CompletableFutures
 * in conjunction with Java Streams to asynchronously search how many
 * times each word in a list of words appears in a list of input
 * strings.
 */
public class SearchWithCompletableFuturesWords
    extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithCompletableFuturesWords(List<String> wordsToFind,
                                             String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream and
     * CompletableFutures to asynchronously search for words in the
     * input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Convert the words to find into a list of CompletableFutures
        // to lists of SearchResults.
        List<CompletableFuture<List<SearchResults>>> listOfFutures = mWordsToFind
            // Create a sequential stream of words to find.
            .stream()

            // Map each word to a CompletableFuture to a list of
            // SearchResults.
            .map(this::processWordAsync)

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
     * Asynchronously search all the input strings for occurrences of
     * the word to find.
     */
    private CompletableFuture<List<SearchResults>> processWordAsync(String word) {
        // Convert the input strings into a list of
        // CompletableFutures to SearchResults.
        List<CompletableFuture<SearchResults>> listOfFutures = getInput()
            // Create a sequential stream of words to find.
            .stream()

            // Map each input string to a CompletableFuture to
            // SearchResults.
            .map(inputString -> {
                    // Get the title.
                    String title = getTitle(inputString);

                    // Get the input string (skipping over the title).
                    String inputData = inputString.substring(title.length());

                    // Asynchronously search for the word in the input string.
                    return CompletableFuture.supplyAsync(() 
                                                         -> searchForWord(word,
                                                                          inputData,
                                                                          title));
                })

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(toList());

        // Return a CompletableFuture to a list of SearchResults that
        // will complete when all the CompletableFutures in the
        // listOfFutures have completed.
        return StreamsUtils.joinAll(listOfFutures);
    }
}
