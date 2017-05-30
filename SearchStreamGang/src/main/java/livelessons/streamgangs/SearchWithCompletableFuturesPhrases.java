package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use CompletableFutures
 * in conjunction with Java Streams to asynchronously search how many
 * times each phrase in a list of phrases appears in a list of input
 * strings.
 */
public class SearchWithCompletableFuturesPhrases
    extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithCompletableFuturesPhrases(List<String> phrasesToFind,
                                               List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream and
     * CompletableFutures to asynchronously search for phrases in the
     * input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Convert the phrases to find into a list of CompletableFutures
        // to lists of SearchResults.
        List<CompletableFuture<List<SearchResults>>> listOfFutures = mPhrasesToFind
            // Create a sequential stream of phrases to find.
            .stream()

            // Map each phrase to a CompletableFuture to a list of
            // SearchResults.
            .map(this::processPhraseAsync)

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(toList());
                    
        // Convert all the completed CompletableFutures in the
        // listOfFutures into a list of lists of SearchResults.
        List<List<SearchResults>> results = StreamsUtils
            .joinAll(listOfFutures)
            // join() blocks the calling thread until all the futures
            // have been completed.
            .join();
            
        // Return results that filter out all zero-sized results.
        return results
            // Convert into a stream.
            .stream()

            // Only keep a result that has at least one match.
            .filter(list -> list.stream().mapToInt(SearchResults::size).sum() > 0)
            
            // Terminate stream and return a list of SearchResults.
            .collect(toList());
    }

    /**
     * Asynchronously search all the input strings for occurrences of
     * the phrase to find.
     */
    private CompletableFuture<List<SearchResults>> processPhraseAsync(String phrase) {
        // Convert the input strings into a list of
        // CompletableFutures to SearchResults.
        List<CompletableFuture<SearchResults>> listOfFutures = getInput()
            // Create a sequential stream of phrases to find.
            .stream()

            // Map each input string to a CompletableFuture to
            // SearchResults.
            .map(input -> {
                    // Get the title.
                    String title = getTitle(input);

                    // Asynchronously search for the phrase in the input string.
                    return CompletableFuture.supplyAsync
                            (()
                             -> searchForPhrase(phrase,
                                                // Get the input string (skipping over the title).
                                                input.subSequence(title.length(), input.length()),
                                                title,
                                                false));
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
