package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

import java.util.List;

import edu.vandy.quoteservices.utils.ArrayUtils;

/**
 * This implementation defines methods that return {@link Flux} of
 * {@link Quote} objects in the database.
 */
public class MultiQueryRepositoryImpl
       implements MultiQueryRepository {
    /**
     * This field is a reactive R2DBC-based client for executing SQL
     * queries against a database using reactive programming
     * constructs, such as Flux and Mono.  It provides a fluent API to
     * build and execute SQL queries in a reactive, non-blocking way.
     */
    @Autowired
    private DatabaseClient databaseClient;

    /**
     * Finds all quotes that contain any of the specified queries.
     *
     * @param queries A {@code List} of {@code String} object
     *               containing the queries to search for
     * @return A {@code Flux} of {@code Quote} objects containing the
     *         quotes that match any of the specified {@code queries}
     *         (ignoring case)
     */
    @Override
    public Flux<Quote> findAllByQuoteContainingAnyIn(List<String> queries) {
        // Create the partial SQL query with a ":params" placeholder.
        String sql =
            "SELECT * FROM quote WHERE LOWER(quote) LIKE :params";

        // Create params that check for any matches of the
        // queries.
        String params = "'%"
            + String.join("%' OR LOWER(quote) LIKE '%",
                          queries)
            .toLowerCase()
            + "%'";

        // Replace the ':params' placeholder with the relevant params.
        sql = sql.replace(":params", params);

        // Use the databaseClient to perform a non-block SQL query.
        return databaseClient
            // Specify the SQL query that will be executed via the
            // 'sql' string.
            .sql(sql)

            // Apply a constructor to each row returned by the query
            // to transform it into a Quote object, which contains the
            // id and quote values from the row.
            .map(row ->
                 new Quote(ArrayUtils
                           // Extract the id value as an Integer.
                           .obj2Number(row.get("id"), Integer.class),

                           // Extract the quote value as a String.
                           row.get("quote", String.class)))

            // This call should not block, but instead will return the
            // Quote objects as they appear and stream them back to
            // the client.
            .all();
    }

    /**
     * Find a {@link Flux} of {@link Quote} objects in the database
     * containing all of the {@code queries} (ignoring case).
     *
     * @param queries {@link List} of {@code queries}
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries} (ignoring
     *         case)
     */
    @Override
    public Flux<Quote> findAllByQuoteContainingAllIn(List<String> queries) {
        // Create the partial SQL query with a ":params" placeholder.
        String sql =
            "SELECT * FROM quote WHERE LOWER(quote) LIKE :params";

        // Create params that check for any matches of the
        // queries.
        String params = "'%"
            + String.join("%' AND LOWER(quote) LIKE '%",
                          queries)
            .toLowerCase()
            + "%'";

        // Replace the ':params' placeholder with the relevant params.
        sql = sql.replace(":params", params);

        // Use the databaseClient to perform a non-block SQL query.
        return databaseClient
            // Specify the SQL query that will be executed via the
            // 'sql' string.
            .sql(sql)

            // Apply a constructor to each row returned by the query
            // to transform it into a Quote object, which contains the
            // id and quote values from the row.
            .map(row ->
                 new Quote(ArrayUtils
                           // Extract the id value as an Integer.
                           .obj2Number(row.get("id"), Integer.class),

                           // Extract the quote value as a String.
                           row.get("quote", String.class)))

            // This call should not block, but instead will return the
            // Quote objects as they appear and stream them back to
            // the client.
            .all();
    }
}
