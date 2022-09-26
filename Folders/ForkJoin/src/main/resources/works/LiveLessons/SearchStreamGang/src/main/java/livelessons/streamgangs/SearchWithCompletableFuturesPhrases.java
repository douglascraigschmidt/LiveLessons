package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import livelessons.utils.ListOfFuturesCollector;
import livelessons.utils.SearchResults;
import livelessons.utils.StreamOfFuturesCollector;
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
        // Convert the phrases to find into a list of lists of SearchResults.
        return mPhrasesToFind
            // Create a sequential stream of phrases to find.
            .stream()

            // Map each phrase to a CompletableFuture to a list of
            // SearchResults.
            .map(this::processPhraseAsync)

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(StreamOfFuturesCollector.toFuture())
                    
            // This completion stage method is called when the future
            // completes (which occurs after all the futures in the
            // stream complete).
            .thenApply(stream -> stream
                       // Only keep a result that has at least one match.
                       .filter(list -> list
                               // Convert to a stream.
                               .stream()
                               
                               // Return the size of each search result.
                               .mapToInt(SearchResults::size)
                               
                               // Add up all the results.
                               .sum() > 0)

                       // Terminate stream and return a list of SearchResults.
                       .collect(toList()))

            // Wait for all the asynchronous processing to complete.
            .join();
    }

    /**
     * Asynchronously search all the input strings for occurrences of
     * the phrase to find.
     */
    private CompletableFuture<List<SearchResults>> processPhraseAsync(String phrase) {
        // Convert the input strings into a list of
        // CompletableFutures to SearchResults.
        return getInput()
            // Create a sequential stream of phrases to find.
            .stream()

            // Map each input string to a CompletableFuture to
            // SearchResults.
            .map(inputSeq -> {
                    // Get the title.
                    String title = getTitle(inputSeq);

                    // Get input string (skip over title).
                    CharSequence input = inputSeq.subSequence(title.length(),
                                                              inputSeq.length());

                    // Asynchronously search for phrase in the input.
                    return CompletableFuture.supplyAsync
                            (() -> searchForPhrase(phrase,
                                                   input,
                                                   title,
                                                   false));
                })

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(ListOfFuturesCollector.toFuture());
    }
}
