package edu.vandy.quoteservices.microservices.zippy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing at least one of
 * the {@code queries} (ignoring case).
 */
@Configuration
public class MultiQueryRepositoryImpl
       implements MultiQueryRepository {
   // @PersistenceUnit
   // private EntityManagerFactory entityManagerFactory;

    /**
     * This field represents a session with the database, providing
     * the main API for performing CRUD (Create, Read, Update, Delete)
     * operations and querying the database.
     */
    @PersistenceContext(unitName = "MultiQueryRepo")
    private EntityManager entityManager;

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
    public List<Quote> findAllByQuoteContainingAllIn(@NonNull List<String> queries) {
        // Get a CriteriaBuilder object from the EntityManager
        // associated with the current JPA transaction and use it to
        // create the criteria query that will be used to search for
        // quotes.
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Create a new criteria query of type Quote that's used to
        // specify the search criteria for the quotes.
        CriteriaQuery<Quote> cq = cb.createQuery(Quote.class);

        // Create a Root object for the Quote entity that specifies
        // the entity to query.
        Root<Quote> quote = cq.from(Quote.class);

        // Create an Expression object that represents the lower-cased
        // ID ("quote") field of the Quote entity that's used to create
        // the search predicate that matches the specified queries.
        var idExpression
                = cb.lower(quote.get("quote"));

        var andPredicate = queries
                // Convert the List to a Stream.
                .stream()

                // Lower case each query.
                .map(String::toLowerCase)

                // Map each query to a "like" predicate that matches the
                // ID (title) field of the Quote entity.
                .map(query -> cb
                        .like(idExpression, "%" + query + "%"))

                // Reduce the list of predicates to a single conjunction
                // (and) predicate.
                .reduce(cb.conjunction(), cb::and);

        return entityManager
                // Create a Query object from the specified criteria
                // query.
                .createQuery(cq
                        // Add the orPredicate to the CriteriaQuery
                        // "where" clause, which returns the quote if
                        // it matches all the specified queries.
                        .where(andPredicate))

                // Execute the JPQL query and return the List of matching
                // Quote objects.
                .getResultList();
    }
}
