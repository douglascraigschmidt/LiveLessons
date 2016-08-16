package livelessons.streamgangs;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

/**
 * Customizes the SearchStreamGangCommon framework to use
 * CompletableFutures in conjunction with Java Streams to search how
 * many times each word in an array of words appears in input data.
 * words.
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
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for words in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        List<CompletableFuture<List<SearchResults>>> listOfFutures = mWordsToFind
            .stream()
            .map(this::processWordAsync)
            .collect(toList());
                    
        // Wait for all operations associated with the futures to
        // complete.  The call to join() is needed here to blocks the
        // calling thread until all the futures have been completed.
        return StreamsUtils.joinAll(listOfFutures).join();
    }

    private CompletableFuture<List<SearchResults>> processWordAsync(String word) {
        List<CompletableFuture<SearchResults>> listOfFutures = getInput()
            .stream()
            .map(inputString -> {
                    String title = getTitle(inputString);
                    String input = inputString.substring(title.length());
                    return CompletableFuture.supplyAsync(() 
                                                         -> searchForWord(word, 
                                                                          input,
                                                                          title));
                })
            .collect(toList());

        // Return a CompletableFuture to a list of SearchResults.
        // that will be available when all the CompletableFutures in
        // the listOfFutures have completed.
        return StreamsUtils.joinAll(listOfFutures);
    }
}
