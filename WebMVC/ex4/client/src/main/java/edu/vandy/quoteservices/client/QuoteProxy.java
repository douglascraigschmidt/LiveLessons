package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static edu.vandy.quoteservices.common.Constants.Service.ZIPPY;

/**
 * This class is a proxy to the {@code GatewayApplication} API gateway
 * and its {@code GatewayController} that use an automatically-generated
 * Retrofit API class.
 */
@Component
public class QuoteProxy {
    /**
     * Create an instance of the {@link QuoteAPI} Retrofit client,
     * which is then used to make HTTP requests synchronously to the
     * {@code GatewayApplication} RESTful microservice.
     */
    @Autowired
    QuoteAPI mQuoteAPI;

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param route The microservice that performs the request
     * @return An {@link List} containing all the {@link Quote}
     *         objects
     */
    public List<Quote> getAllQuotes(String route) {
        try {
            // Execute the getAllQuotes() retrofit API method.
            var response = mQuoteAPI
                .getAllQuotes(route).execute();

            // Determine whether this method succeeded or failed.
            if (response.isSuccessful())
                return response.body();
            else {
                assert response.errorBody() != null;
                System.out.println(response.errorBody().string());

                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param route The microservice that performs the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Get the {@code quoteIds} in parallel if true,
     *                 else run sequentially
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> postQuotes
        (String route,
         List<Integer> quoteIds,
         Boolean parallel) {
        try {
            // Execute the getQuotes() retrofit API method.
            var response = mQuoteAPI
                .postQuotes(route,
                           quoteIds,
                           parallel).execute();

            // Determine whether this method succeeded or failed.
            if (response.isSuccessful())
                return response.body();
            else {
                assert response.errorBody() != null;
                System.out.println(response.errorBody().string());

                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries}.
     *
     * @param route The microservice that performs the request
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @param parallel Search for the {@code queries} in parallel if
     *                 true, else run sequentially
     * @return A {@link List} of {@link Quote} objects that match the
     *         queries
     */
    public List<Quote> search
        (String route,
         List<String> queries,
         Boolean parallel) {
        try {
            // Execute the search() retrofit API method.
            var response = mQuoteAPI
                .search(route,
                        queries,
                        parallel).execute();

            // Determine whether this method succeeded or failed.
            if (response.isSuccessful())
                return response.body();
            else {
                assert response.errorBody() != null;
                System.out.println(response.errorBody().string());

                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search the Zippy microservice for quotes containing all the
     * given {@link List} of {@code queries} using a custom SQL method.
     *
     * @param route The microservice that performs the request
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @param parallel Search for the {@code queries} in parallel if
     *                 true, else run sequentially
     * @return A {@link List} of {@link Quote} objects that match the
     *         queries
     */
    public List<Quote> searchEx
        (String route,
         List<String> queries,
         Boolean parallel) {
        try {
            // Execute the searchEx() retrofit API method.
            var response = mQuoteAPI
                .searchEx(route,
                          queries, parallel).execute();

            // Determine whether this method succeeded or failed.
            if (response.isSuccessful())
                return response.body();
            else {
                assert response.errorBody() != null;
                System.out.println(response.errorBody().string());

                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
