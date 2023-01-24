package berraquotes.server.strategies;

import berraquotes.common.Quote;
import berraquotes.utils.FutureUtils;
import jdk.incubator.concurrent.StructuredTaskScope;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This strategy uses the Java structured concurrency framework to
 * provide Berra quotes.
 */
public class BQStructuredConcurrencyStrategy
       extends BQAbstractStrategy {
    /**
     * Max size of each batch.
     */
    private static final int sBATCH_SIZE = 10;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        try (var scope =
             // Create a new StructuredTaskScope that shuts down on
             // failure.
             new StructuredTaskScope.ShutdownOnFailure()) {
            // Get a List of Futures to Quote objects that are being
            // processed in parallel.
            var results = quoteIds
                // Convert the List to a Stream.
                .stream()

                // Asynchronously determine if the quote matches any
                // of the search queries.
                .map(quoteId ->
                     scope.fork(() -> mQuotes
                                // Get the quote associated with the
                                // quoteId.
                                .get(quoteId)))

                // Convert the Stream to a List.
                .toList();

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            scope.join();

            // Throw an Exception upon failure of any tasks.
            scope.throwIfFailed();

            // Return a List of Quote objects.
            return FutureUtils
                // Convert the List of Future<Quote> objects to a
                // Stream of Quote objects.
                .futures2List(results);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Convert the {@link List} parameter into a {@link Stream} of
     * sublists, each of which has at most {@code batchSize} elements.
     *
     * @param list The {@link List} to split into batches
     * @param batchSize The maximum size of each batch
     * @return A {@link Stream} of sublists, each of which has
     *         at most {@code batchSize} elements
     */
    public static <T> Stream<List<T>> getBatches(List<T> list,
                                                 int batchSize) {
        return IntStream
            // Create an iterator that will traverse the List.
            .iterate(0,
                     i -> i < list.size(),
                     i -> i + batchSize)
            // Split the original List into sublists, each of
            // which has at most batchSize elements.
            .mapToObj(i -> list
                      .subList(i, Math
                               .min(i + batchSize,
                                    list.size())));
    }

    /**
     * Search for quotes containing the given {@link String} {@code
     * query} and return a {@link List} of matching {@link Quote}
     * objects.
     *
     * @param query The search query
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code query}
     */
    @Override
    public List<Quote> search(String query) {
        try (var scope =
             // Create a new StructuredTaskScope that shuts down on
             // failure.
             new StructuredTaskScope.ShutdownOnFailure()) {
            // Get a List of Futures to Quote objects that are being
            // processed in parallel.
            var results = BQStructuredConcurrencyStrategy
                // Split the List into a sublist.
                .getBatches(mQuotes,
                            sBATCH_SIZE)

                // Asynchronously determine if the quote matches any
                // of the search queries.
                .map(batch -> scope
                     .fork(() ->
                           findMatch(query, batch)))

                // Convert the Stream to a List.
                .toList();

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            scope.join();

            // Throw an Exception upon failure of any tasks.
            scope.throwIfFailed();

            // Return a List of Quote objects.
            return getQuoteList(results);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Search for quotes containing the given {@link String} {@code
     * queries} and return a {@link List} of matching {@link Quote}
     * objects.
     *
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(List<String> queries) {
        // Use Java structured concurrency to locate all quotes whose
        // 'quote' field matches the List of 'queries' and return them
        // as a List of Quote objects.
        try (var scope =
             // Create a new StructuredTaskScope that shuts down on
             // failure.
             new StructuredTaskScope.ShutdownOnFailure()) {

            // Get a List of Futures to List<Quote> objects that
            // are being processed in parallel.
            var results =
                getFutureList(mQuotes,
                              queries,
                              scope);

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            scope.join();

            // Throw an Exception upon failure of any tasks.
            scope.throwIfFailed();

            // Return a List of Quote objects.
            return getQuoteList(results);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Asynchronously determine which {@link Quote} {@link String}
     * match any of the {@code queries}.
     *
     * @param quotes The {@link List} of {@link Quote} objects
     * @param queries The queries to search for
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link List} of {@link Future} objects containing a
     *         {@link List} with {@link Quote} objects if there's a
     *         match or an empty {@link List} if there's no match
     */
    @NotNull
    private List<Future<List<Quote>>> getFutureList
        (List<Quote> quotes,
         List<String> queries,
         StructuredTaskScope.ShutdownOnFailure scope) {
        return BQStructuredConcurrencyStrategy
            // Split the List into a sublist.
            .getBatches(quotes,
                        sBATCH_SIZE)

            // Asynchronously determine if the quote matches any of
            // the search queries.
            .map(quoteBatch ->
                 findMatchAsync(quoteBatch, queries, scope))

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Get a {@link List} of {@link Quote} objects that matched at
     * least one query.
     *
     * @param results A {@link List} of {@link Future} objects
     *                containing a {@link List} with {@link
     *                Quote} objects if there's a match or an
     *                empty {@link List} if there's no match
     * @return A {@link List} of {@link Quote} objects
     */
    @NotNull
    private List<Quote> getQuoteList
        (List<Future<List<Quote>>> results) {
        return FutureUtils
            // Convert the List of Future<Optional<Quote>> objects to
            // a Stream of Optional<Quote> objects.
            .futures2Stream(results)

            // Eliminate empty Optional objects.
            .flatMap(List::stream)

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Asynchronously determine if the {@code quote} matches with the
     * {@link List} of {@code queries}.
     *
     * @param quotes The {@link List} of {@link Quote} objects
     *               to match against
     * @param queries The search queries
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link Future} to an {@link List} containing any
     *         {@link Quote} objects that match or an empty {@link
     *         List} if there's no match
     */
    private Future<List<Quote>> findMatchAsync
        (List<Quote> quotes,
         List<String> queries,
         StructuredTaskScope.ShutdownOnFailure scope) {
        return scope
            // Create a virtual thread.
            .fork(() -> queries
                  // Convert the List to a Stream.
                  .stream()

                  // Determine if there's any match of the 'query' in
                  // the sublist of quotes.
                  .map(query ->
                       findMatch(query,
                                 quotes))

                  // Flatten the Stream of List<Quote> objects to a
                  // Stream of Quote objects.
                  .flatMap(List::stream)

                  // Convert the Stream to a List.
                  .toList());
    }

    /**
     * Find a match between the {@code query} and the {@code quote}.
     *
     * @param query The query 
     * @param quotes The {@link List} of {@link Quote} objects
     *               to match with
     * @return True if there's a match, else false
     */
    private List<Quote> findMatch(String query,
                                  List<Quote> quotes) {
        return quotes
            // Convert the List to a Stream.
            .stream()

            // Keep any Quote that matches the query.
            .filter(quote -> quote.quote().toLowerCase()
                    .contains(query.toLowerCase()))

            // Convert the List to a Stream.
            .toList();
    }
}
