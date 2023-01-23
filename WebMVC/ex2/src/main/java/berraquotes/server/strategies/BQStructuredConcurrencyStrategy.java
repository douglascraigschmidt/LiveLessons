import berraquotes.common.Quote;
import berraquotes.server.strategies.BQAbstractStrategy;
import berraquotes.utils.FutureUtils;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 */
public class BQStructuredConcurrencyStrategy
       extends BQAbstractStrategy {
    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the Handey quote associated with the quoteId.
            .map(quoteId -> mQuotes.get(quoteId))

            // Trigger intermediate operations and collect the results
            // into a List.
            .toList();
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(List<String> queries) {
        try (var scope =
             new StructuredTaskScope.ShutdownOnFailure()) {
            var results = getFutureList(queries,
                                               scope);

            scope.join();
            scope.throwIfFailed();
            return getQuoteList(results);
        } catch (Exception ex) {
          System.out.println("exception = " + ex.getMessage());
          throw new RuntimeException(ex);
        }
    }

    private List<Future<Optional<Quote>>> getFutureList(List<String> queries,
                                                        StructuredTaskScope.ShutdownOnFailure scope) {
        return mQuotes
                // Convert the List to a Stream.
                .stream()

                // Asynchronously determine if the quote matches
                // any of the search queries.
                .map(quote ->
                        findMatchAsync(quote, queries, scope))

                // Convert the Stream to a List.
                .toList();
    }

    private List<Quote> getQuoteList(List<Future<Optional<Quote>>> results) {
        return FutureUtils
                // Convert the List of Future<Optional<Movie>> objects to
                // a Stream of Optional<Movie> objects.
                .futures2Stream(results)

                // Eliminate empty Optional objects.
                .flatMap(Optional::stream)

                // Convert the Stream to a List.
                .toList();
    }

    private Future<Optional<Quote>> findMatchAsync(Quote quote,
                                                   List<String> queries,
                                                   StructuredTaskScope.ShutdownOnFailure scope) {
        return scope
                // Create a virtual thread.
                .fork(() -> Optional
                        // Create an empty Optional if there's no match,
                        // else the Optional contains the movie that
                        // matches.
                        .ofNullable(queries
                                // Convert the List to a Stream.
                                .stream()
                                // Determine if there's any match.
                                .anyMatch(query ->
                                        findMatch(query,
                                                quote.quote()))
                                ? quote
                                : null));

    }

    private boolean findMatch(String query, String quote) {
        return quote.toLowerCase().contains(query.toLowerCase());
    }
}
