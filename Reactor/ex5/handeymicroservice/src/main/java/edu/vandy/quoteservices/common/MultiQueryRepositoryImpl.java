package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.quoteservices.utils.ArrayUtils.obj2Number;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing at least one of
 * the {@code queries} (ignoring case).
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

    @Override
    public Flux<Quote> findAllByQuoteContainingAnyIn(List<String> queries) {
        String sql =
            "SELECT * FROM quote WHERE LOWER(quote) LIKE $1";

        String params = "'%"
            + String.join("%' OR LOWER(quote) LIKE '%",
                queries)
            .toLowerCase()
            + "%'";

        sql = sql.replace("$1", params);
        return databaseClient
            .sql(sql)
            .map(row ->
                new Quote(obj2Number(row.get("id"), Integer.class),
                     row.get("quote", String.class)))
            .all();
    }

    /**
     * Find a {@link Flux} of {@link Quote} objects in the database
     * containing all of the {@code queries} (ignoring case).
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    @Override
    public Flux<Quote> findAllByQuoteContainingAllIn(List<String> queries) {
        String sql =
            "SELECT * FROM quote WHERE LOWER(quote) LIKE $1";

        String params = "'%"
            + String.join("%' AND LOWER(quote) LIKE '%",
                queries)
            .toLowerCase()
            + "%'";

        sql = sql.replace("$1", params);
        return databaseClient
            .sql(sql)
            .map(row ->
                new Quote(obj2Number(row.get("id"), Integer.class),
                         row.get("quote", String.class)))
            .all();
    }
}
