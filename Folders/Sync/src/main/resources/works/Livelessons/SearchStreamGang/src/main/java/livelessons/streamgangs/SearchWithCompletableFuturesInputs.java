package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamOfFuturesCollector.toFuture;

/**
 * Customizes the SearchStreamGang framework to use CompletableFutures
 * in conjunction with Java streams to asynchronously search the input
 * data for each phrase in an list of phrases.
 */
public class SearchWithCompletableFuturesInputs
    extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithCompletableFuturesInputs(List<String> phrasesToFind,
                                              List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java 8 Stream in
     * conjunction with CompletableFutures to asynchronously search for
     * phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        return getInput()
                // Create a sequential stream of input strings.
                .stream()

                // Map each input string to a CompletableFuture to a list
                // of SearchResults.
                .map(this::processInputAsync)

                // Trigger intermediate operations and return a single
                // completable future to a stream of completable futures.
                .collect(toFuture())

                // This completion stage method is called when the future
                // completes (which occurs after all the futures in the
                // stream complete).
                .thenApply(stream -> stream
                           // Only keep a result that has at least one match.
                           .filter(list -> list
                                   // Conver to a stream.
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
     * Asynchronously search @a inputString for all occurrences of the
     * phrases to find.
     */
    private CompletableFuture<List<SearchResults>> processInputAsync(CharSequence inputSeq) {
        // Store the title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input =
            inputSeq.subSequence(title.length(),
                                 inputSeq.length());

        // Convert the list of phrases into a list of CompletableFutures
        // to SearchResults.
        List<CompletableFuture<SearchResults>> listOfFutures = mPhrasesToFind
            // Create a sequential stream of phrases.
            .stream()

            // Asynchronously find each phrase in the input data and
            // return a CompletableFuture<SearchResults>.
            .map(phrase ->
                 CompletableFuture.supplyAsync(()
                                               -> searchForPhrase(phrase,
                                                                  input,
                                                                  title,
                                                                  false)))

            // Terminate stream and return a list of
            // CompletableFutures.
            .collect(toList());

        // Return a CompletableFuture to a list of SearchResults that
        // will be complete when all the CompletableFutures in the
        // listOfFutures have completed.
        return StreamsUtils.joinAll(listOfFutures);
    }
}

