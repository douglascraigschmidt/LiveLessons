package edu.vandy.quoteservices.repository;

import edu.vandy.quoteservices.common.Quote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing at least one of
 * the {@code queries} (ignoring case).
 */
public class MultiQueryRepositoryImpl
      implements MultiQueryRepository {
    /**
     * This field represents a session with the database that provides
     * the main API for performing CRUD (Create, Read, Update, Delete)
     * operations and querying the database.
     */
    @PersistenceContext
    private EntityManager mEntityManager;

    /**
     * Find a {@link List} of {@link Quote} objects in the database
     * containing all of the {@code queries} (ignoring case).
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    @Override
    public List<Quote> findAllByQuoteContainingIgnoreCaseAllIn
        (List<String> queries) {
        // Get a CriteriaBuilder object from the EntityManager
        // associated with the current JPA transaction and use it to
        // create the criteria query that will be used to search for
        // quotes.
        var criteriaBuilder = mEntityManager.getCriteriaBuilder();

        // Create a new criteria query of type Quote that's used to
        // specify the search criteria for the quotes.
        var criteriaQuery = criteriaBuilder
            .createQuery(Quote.class);

        // Create a Root object for the Quote entity that specifies
        // the entity to query.
        var quote = criteriaQuery.from(Quote.class);

        // Create an Expression object that represents the lower-cased
        // quote ("quote") column/field of the Quote entity that's used to
        // create the search predicate that matches the specified queries.
        var quoteExpression = criteriaBuilder
            .lower(quote.get("quote"));

        // Get a Predicate that "ands" all the queries together.
        var andPredicate = getPredicate(queries,
                                        criteriaBuilder,
                                        quoteExpression);

        // Perform the query and return the results.
        return getQueryResults(criteriaQuery,
                               andPredicate,
                               quoteExpression);
    }

    /**
     * Get a {@link Predicate} that "ands" all the {@code queries}
     * together.
     * 
     * @param queries The {@link List} of queries
     * @param criteriaBuilder Create the {@link CriteriaQuery} used to
     *                        search for quotes
     * @param idExpression The lower-cased "quote" column of the
     *                        {@link Quote} entity
     * @return A {@link Predicate} that "ands" all the {@code queries}
     *         together
     */
    public static Predicate getPredicate
        (List<String> queries,
         CriteriaBuilder criteriaBuilder,
         Expression<String> idExpression) {
        // Use a Java sequential stream to build a Predicate that
        // "ands" all the lower-cased queries together.
        return queries
            // Convert the List to a Stream.
            .stream()

            // Lower case each query.
            .map(String::toLowerCase)

            // Map each query to a "like" predicate that matches the
            // 'quote' field of the Quote entity.
            .map(query -> criteriaBuilder
                 .like(idExpression,
                       "%" + query + "%"))

            // Reduce the list of predicates to a single conjunction
            // (and) predicate.
            .reduce(criteriaBuilder.conjunction(), 
                    criteriaBuilder::and);
    }

    /**
     * Perform the query and return the results.
     * 
     * @param criteriaQuery A {@link CriteriaQuery} of type {@link
     *                       Quote} that specifies the search criteria
     *                       for the quotes
     * @param andPredicate A {@link Predicate} that "ands" all the
     *                     queries together
     * @param quoteExpression The lower-cased "quote" column of the
     *                        {@link Quote} entity
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    public List<Quote> getQueryResults
        (CriteriaQuery<Quote> criteriaQuery,
         Predicate andPredicate,
         Expression<String> quoteExpression) {
        // Create and execute a query that returns a List of
        // non-duplicate Quote objects ordered by the 'quote' field,
        // where the Quote's 'quote' field contains all given queries.

        return mEntityManager
            // Create a Query object from the specified criteria
            // query.
            .createQuery(criteriaQuery
                         // Add the andPredicate to the CriteriaQuery
                         // "where" clause which returns each Quote
                         // that matches all the specified queries.
                         .where(andPredicate)

                         // Group the results by the quoteExpression
                         // field to ensure non-duplicate results;
                         .groupBy(quoteExpression))

            // Execute the query and return the result as a List.
            .getResultList();
    }
}
