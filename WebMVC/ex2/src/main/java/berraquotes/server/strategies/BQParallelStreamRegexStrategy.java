package berraquotes.server.strategies;

import berraquotes.common.Quote;

import java.util.List;

/**
 * This strategy uses the Java parallel streams framework and regular
 * expression matching to provide Berra quotes.
 */
public class BQParallelStreamRegexStrategy
       extends BQParallelStreamStrategy {
    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(List<String> queries) {
        // Combine the 'queries' List into a lowercase String and
        // convert into a regex of style
        // (.*{query_1}.*)|(.*{query_2}.*)...(.*{query_n}.*)

        String regexQueries = queries
            // toString() returns the values as a comma-separated
            // string enclosed in square brackets.
            .toString()

            // Lowercase for matching purposes.
            .toLowerCase()

            // Start of regex.
            .replace("[","(.*")

            // Separators between queries previous operations added in
            // a space with each comma.
            .replace(", ",".*)|(.*")

            // End of regex.
            .replace("]",".*)");

        return mQuotes
            // Convert the List to a Stream.
            .parallelStream()

            // Keep all quotes that match the regex.
            .filter(quote -> quote.quote()
                    .toLowerCase()
                    // Execute the regex portion of the filter.
                    .matches(regexQueries))

            // Convert the Stream to a List.
            .toList();
    }
}
