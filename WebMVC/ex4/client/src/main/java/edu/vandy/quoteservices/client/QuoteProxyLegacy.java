package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;
import static edu.vandy.quoteservices.common.Constants.Params.PARALLEL;
import static edu.vandy.quoteservices.common.Constants.Params.QUOTE_ID;

/**
 * This class is a proxy to the {@code GatewayApplication} API gateway
 * and its {@code GatewayController} that use Spring's
 * UriComponentsBuilder and RestTemplate to interact with the API.
 */
@Component
public class QuoteProxyLegacy {
    /**
     * This field connects the {@link QuoteProxyLegacy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param route The microservice that performs the request
     * @return An {@link List} containing all the {@link Quote}
     *         objects
     */
    public List<Quote> getAllQuotes(String route) {
        // Build the URL.
        String uri = UriComponentsBuilder
            .fromPath(route + "/" + GET_ALL_QUOTES)
            .build()
            .toUriString();

        // Invoke the remote call and return the result.
        return WebUtils
            .makeGetRequestList(mRestTemplate,
                                uri,
                                Quote[].class);
    }

    /**
     * Get a {@link Quote} corresponding to the given id.
     *
     * @param quoteId An {@link Integer} containing the given
     *                 {@code quoteId}
     * @return A {@link Quote} containing the requested {@code quoteId}
     */
    public Quote getQuote(String route,
                          Integer quoteId) {
        // Create the encoded URI.
        String uri = UriComponentsBuilder
            .fromPath(route + "/" + GET_QUOTE)
            .queryParam(QUOTE_ID, quoteId)
            .build()
            .toUriString();

        // Invoke the remote call and return the result.
        return WebUtils
            .makeGetRequest(mRestTemplate,
                                uri,
                                Quote.class);
    }

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param route The microservice that performs the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Get the {@code quoteIds} in parallel if true,
     *                 else run sequentially
     * @return An {@link List} containing the requested {@link Quote}
     *         objects
     */
    public List<Quote> postQuotes
        (String route,
         List<Integer> quoteIds,
         Boolean parallel) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            .fromPath(route + "/" + POST_QUOTES)
            .queryParam(PARALLEL, parallel)
            .build()
            .toUriString();

        // Invoke the remote call and return the result.
        return WebUtils
            .makePostRequestList(mRestTemplate,
                                 uri,
                                 quoteIds,
                                 Quote[].class);
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries}.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @param parallel Search for the {@code queries} in parallel if
     *                 true, else run sequentially, which is passed
     *                 as part of the URL
     * @return A {@link List} containing all the {@link Quote} objects
     *         on success and an error message on failure
     */
    List<Quote> search(String routename,
                       List<String> queries,
                       Boolean parallel) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            .fromPath(routename + "/" + POST_SEARCHES)
            .queryParam(PARALLEL, parallel)
            .build()
            .toUriString();

        // Invoke the remote call and return the result.
        return WebUtils
            .makePostRequestList(mRestTemplate,
                                 uri,
                                 queries,
                                 Quote[].class);
    }

    /**
     * Search for quotes containing all the given {@link List} of
     * {@code queries}.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @param parallel Search for the {@code queries} in parallel if
     *                 true, else run sequentially, which is passed as
     *                 part of the URL
     * @return A {@link List} containing the {@link Quote} objects on
     *         success and an error message on failure
     */
    List<Quote> searchEx(String routename,
                         List<String> queries,
                         Boolean parallel) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            .fromPath(routename + "/" + POST_SEARCHES_EX)
            .queryParam(PARALLEL, parallel)
            .build()
            .toUriString();

        // Invoke the remote call and return the result.
        return WebUtils
            .makePostRequestList(mRestTemplate,
                                 uri,
                                 queries,
                                 Quote[].class);
    }
}

