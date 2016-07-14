package streamgangs;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import utils.SearchResults;
import utils.StreamsUtils;

/**
 * Customizes the SearchStreamGangCommon framework to use a parallel
 * Java Stream to search the input data for each word in an array of
 * words.
 */
public class SearchWithCompletableFuturesWords
    extends SearchStreamGangAsync {
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
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for words in the input data.
     */
    @Override
    protected List<List<CompletableFuture<SearchResults>>> processStream() {
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        final List<CompletableFuture<List<CompletableFuture<SearchResults>>>> listOfFutures = mWordsToFind
            .parallelStream()

            .map(this::processWordAsync)

            // Terminate the stream.
            .collect(toList());

        // Wait for all operations associated with the futures to
        // complete.
        final CompletableFuture<List<List<CompletableFuture<SearchResults>>>> allDone =
                StreamsUtils.joinAll(listOfFutures);
  
        // The call to join() is needed here to blocks the calling
        // thread until all the futures have been completed.
        return allDone.join();
    }

    /**
     * Search the inputData for all occurrences of the words to find.
     */
    protected CompletableFuture<List<CompletableFuture<SearchResults>>> processWordAsync(String word) {
        // Get the input.
        List<CompletableFuture<SearchResults>> listOfFutures = getInput()
            // Sequentially process each String in the input list.
            .parallelStream()

            // Map each String to a Stream containing the words found
            // in the input.
            .map(inputString -> {
                    // Get the section title.
                    String title = getTitle(inputString);

                    return CompletableFuture.supplyAsync(() 
                                                         -> searchForWord(word, 
                                                                          // Skip over the title.
                                                                          inputString.substring(title.length()),
                                                                          title));
                })
            
            // Only keep a result that has at least one match.
            // .filter(resultFuture -> resultFuture.thenApply(result -> result.size() > 0))

            .collect(toList());

        // Create a future to hold the results.
        CompletableFuture<List<CompletableFuture<SearchResults>>> future =
            new CompletableFuture<>();
        future.complete(listOfFutures);
        return future;
    }
}

