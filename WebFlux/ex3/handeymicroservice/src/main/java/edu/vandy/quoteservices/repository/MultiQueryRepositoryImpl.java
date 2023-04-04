package edu.vandy.quoteservices.repository;

import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This implementation defines methods that return a {@link Flux} that
 * emits {@link Quote} objects in the database containing all or any
 * of the queries (ignoring case).
 */
public class MultiQueryRepositoryImpl
    implements MultiQueryRepository {
    /**
     * This field is a reactive R2DBC-based client for executing SQL
     * queries against a database using reactive programming
     * constructs, such as {@link Flux} and {@link Mono}.  It provides
     * a fluent API to build and execute SQL queries in a reactive,
     * non-blocking way.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DatabaseClient mDatabaseClient;

    /**
     * Find a {@link Flux} of {@link Quote} objects in the database
     * containing all the {@code queries} (ignoring case).
     *
     * @param queries A {@code List} of {@code String} objects
     *                containing the queries to search for
     * @return A {@link Flux} that emits {@link Quote} objects in the
     * database containing all the {@code queries} (ignoring
     * case)
     */
    @Override
    public Flux<Quote> findAllByQuoteContainingIgnoreCaseAllIn
    (List<String> queries) {
        // Build an SQL query String that will match Quote objects in
        // the database containing all the queries.
        String sql = buildQueryString
            ("SELECT * FROM quote WHERE LOWER(quote) LIKE :params",
                "%' AND LOWER(quote) LIKE '%",
                queries);

        // Perform the SQL query and return a Flux of matching Quote
        // objects.
        return getQuoteFlux(sql);
    }

    /**
     * Return a {@link Flux} that emits {@link Quote} objects in the
     * database containing any of the {@code queries} (ignoring case).
     *
     * @param queries A {@code List} of {@code String} objects
     *                containing the queries to search for
     * @return A {@code Flux} that emits {@code Quote} objects that
     * match any of the specified {@code queries} (ignoring
     * case)
     */
    @Override
    public Flux<Quote> findAllByQuoteContainingIgnoreCaseAnyIn
    (List<String> queries) {
        // Build an SQL query String that will match Quote objects in
        // the database containing all the queries.
        String sql = buildQueryString
            ("SELECT * FROM quote WHERE LOWER(quote) LIKE :params",
                "%' OR LOWER(quote) LIKE '%",
                queries);

        // Perform the SQL query and return a Flux of matching Quote
        // objects.
        return getQuoteFlux(sql);
    }

    /**
     * This factory method builds an SQL query string from the
     * provided parameters.
     *
     * @param sqlString   The SQL {@link String} to modify by replacing
     *                    the ':params' placeholder with wildcard values
     * @param whereFilter The filter for the {@code sqlString} to use
     *                    when concatenating the {@link List} of {@code
     *                    queries}
     * @param queries     The {@link List} of query {@link String} objects
     *                    to concatenate and use as wildcard values
     * @return A modified SQL query {@link String} with the ':params'
     * placeholder replaced by a {@link String} of wildcard values
     */
    public static String buildQueryString(String sqlString,
                                          String whereFilter,
                                          List<String> queries) {
        // Return a String that replaces the ':params' placeholder in
        // the 'sqlString' param with a String of concatenated
        // wildcard values based on the input List of queries.

        // Create a params String that filters the queries according
        // to 'whereFilter' in a case-insensitive manner.
        String expandedParams =
            "'%" + String
                .join(whereFilter,
                    queries).toLowerCase() + "%'";

        // Return the sqlString.
        return sqlString
            // Replace the ':params' placeholder with the params
            // String.
            .replace(":params", expandedParams);
    }

    /**
     * Execute an R2DBC SQL query against the {@link Quote} database
     * and map the resulting rows to a {@link Flux} that emits objects
     * of the {@link Quote} class.
     *
     * @param sql the SQL query string to execute against the database
     * @return A {@link Flux} that emits {@link Quote} objects, each
     * representing a row in the query result set that match
     * the {@code sql} query
     */
    public Flux<Quote> getQuoteFlux(String sql) {
        // Use the mDatabaseClient to perform a reactive SQL query and
        // return a Flux that emits matching Quote objects.
        return mDatabaseClient
            // Specify the SQL query that will be executed via the
            // 'sql' string.
            .sql(sql)

            // Apply a constructor to each row returned by the query
            // to transform it into a Quote object, which contains the
            // id and quote values from the row.
            .map(row ->
                new Quote(ArrayUtils
                    // Extract 'id' as an Integer
                    .obj2Number(row.get("id"), Integer.class),

                    // Extract the 'quote' value as a String.
                    row.get("quote", String.class)))

            // This call should not block, but instead will return the
            // Quote objects as they appear and stream them back to
            // the client.
            .all();
    }
}
