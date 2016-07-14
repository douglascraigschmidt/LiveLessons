import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Customizes the SearchStreamGangCommon framework to use a parallel
 * Java Stream to search the input data for each word in an array of
 * words.
 */
public class SearchWithCompletableFuturesInputs
             extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    SearchWithCompletableFuturesInputs(List<String> wordsToFind,
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
    protected List<SearchResults> processStream() {
        // Note the start time.
        long start = System.nanoTime();

        // Get the input.
        List<CompletableFuture<List<CompletableFuture<SearchResults>>>> listOfFutures = getInput()
            // Sequentially process each String in the input list.
            .parallelStream()

            // Map each String to a Stream containing the words found
            // in the input.
            .map(this::processInputAsync)
            
            // Only keep a result that has at least one match.
            // .filter(resultFuture -> resultFuture.thenApply(result -> result.size() > 0))

            .collect(toList());

        // Wait for all operations associated with the futures to
        // complete.
        final CompletableFuture<List<List<CompletableFuture<SearchResults>>>> allDone =
                joinAll(listOfFutures);
        // Print the processing time.
        System.out.println(TAG + 
                           ": Done in " 
                           + (System.nanoTime() - start) / 1_000_000
                           + " msecs");
        // The call to join() is needed here to blocks the calling
        // thread until all the futures have been completed.

        List<List<CompletableFuture<SearchResults>>> results = allDone.join();
        System.out.println(TAG + ": The search returned " 
                           + results.stream().mapToInt(list -> list.stream().mapToInt(future -> future.join().size()).sum()).sum()
                           + " word matches");
        return null;
    }

    /**
     * Search the inputData for all occurrences of the words to find.
     */
    protected CompletableFuture<List<CompletableFuture<SearchResults>>> processInputAsync(String input) {
        // Get the section title.
        final String title = getTitle(input);

        // Skip over the title.
        final String inputString = input.substring(title.length());

        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        final List<CompletableFuture<SearchResults>> listOfFutures = mWordsToFind
            .parallelStream()

            .map(word -> {
                    return CompletableFuture.supplyAsync(() 
                                                         -> searchForWord(word,
                                                                          inputString,
                                                                          title));
                })

            // Terminate the stream.
            .collect(toList());

        // Create a future to hold the results.
        CompletableFuture<List<CompletableFuture<SearchResults>>> future =
            new CompletableFuture<>();
        future.complete(listOfFutures);
        return future;
    }

    public static <T> CompletableFuture<List<T>> joinAll(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                                       futures.stream()
                                       .map(CompletableFuture::join)
                                       .collect(toList()));
    }
}

