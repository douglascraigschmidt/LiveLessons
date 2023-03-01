package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.Quote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * This class realizes the "Repository Implementation Pattern" to
 * define a method that returns a {@link List} of {@link Quote}
 * objects in the database containing at least one of the {@code
 * queries} (ignoring case).
 */
public class MultiQueryRepositoryImpl
       implements MultiQueryRepository {
    /**
     * This field represents a session with the database, providing
     * the main API for performing CRUD (Create, Read, Update, Delete)
     * operations and querying the database.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find a {@link List} of {@link Quote} objects in the database
     * containing all of the {@code queries} (ignoring case).
     *
     * @param queries The {@link List} of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    @Override
    public List<Quote> findAllByQuoteContainingIgnoreCaseAllIn
        (@NonNull List<String> queries) {
        // Get a CriteriaBuilder object from the EntityManager
        // associated with the current JPA transaction and use it to
        // create the CriteriaQuery used to search for quotes.
        var criteriaBuilder = entityManager
            .getCriteriaBuilder();

        // Create a new CriteriaQuery of type Quote used to specify
        // the search criteria for the quotes.
        var criteriaQuery = criteriaBuilder
            .createQuery(Quote.class);

        // Create a Root object for the Quote entity that specifies
        // the entity to query.
        var quote = criteriaQuery
            .from(Quote.class);

        // Create an Expression object that represents the lower-cased
        // "quote" column of the Quote entity used to create the
        // search predicate that matches the specified queries.
        var quoteExpression = criteriaBuilder
            .lower(quote.get("quote"));

        // Get a Predicate that "ands" all the queries together.
        var andPredicate =
            getPredicate(queries, criteriaBuilder, quoteExpression);

        // Perform the query and return the results.
        return getQueryResults(criteriaQuery, andPredicate);
    }

    /**
     * Get a {@link Predicate} that "ands" all the {@code queries}
     * together.
     * 
     * @param queries The {@lin List} of queries
     * @param criteriaBuilder Create the {@link CriteriaQuery} used to
     *                        search for quotes

     * @param quoteExpression The lower-cased "quote" column of the
     *                        {@link Quote} entity
     * @return A {@link Predicate} that "ands" all the {@code queries}
     *         together
     */
    private static Predicate
        getPredicate(List<String> queries,
                     CriteriaBuilder criteriaBuilder,
                     Expression<String> quoteExpression) {
        return queries
            // Convert the List to a Stream.
            .stream()

            // Lower case each query.
            .map(String::toLowerCase)

            // Map each query to a "like" predicate that matches the
            // "quote" column of the Quote entity.
            .map(query -> criteriaBuilder
                 .like(quoteExpression,
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
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    private List<Quote> getQueryResults(CriteriaQuery<Quote> criteriaQuery,
                                        Predicate andPredicate) {
        return entityManager
            // Create a Query object from the specified criteria
            // query.
            .createQuery(criteriaQuery
                         // Add the andPredicate to the CriteriaQuery
                         // "where" clause, which returns the quote if
                         // it matches all the specified queries.
                         .where(andPredicate))

            // Execute the JPQL query and return the List of matching
            // Quote objects.
            .getResultList();
    }
}
