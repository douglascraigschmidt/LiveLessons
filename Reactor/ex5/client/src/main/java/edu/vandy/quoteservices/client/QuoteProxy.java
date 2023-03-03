package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;
import static edu.vandy.quoteservices.common.Constants.Service.ZIPPY;

/**
 * This class is a proxy to the {@code GatewayApplication} API gateway
 * and its {@code GatewayController} that use an automatically-generated
 * Retrofit API class.
 */
@Component
public class QuoteProxy {
    /**
     * Create an instance of the {@link WebClient} client, which is
     * then used to make HTTP requests asynchronously to the {@code
     * GatewayApplication} RESTful microservice.
     */
    @Autowired
    WebClient mWebClient;

    /**
     * Get a {@link Flux} that emits the requested quotes.
     *
     * @param route The microservice that performs the request
     * @return An {@link Flux} that emits all the {@link Quote}
     *         objects
     */
    public Flux<Quote> getAllQuotes(String route) {
        // Use WebUtils to create a URL to the GET_ALL_QUOTES endpoint
        // of the 'route' microservice.
        var uri = WebUtils
            .buildUriString(route + "/" + GET_ALL_QUOTES);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching quotes from the 'route' microservice.
            .makeGetRequestFlux(mWebClient,
                                uri,
                                Quote.class);
    }

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param route The microservice that performs the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link Flux} that emits the requested {@link Quote}
     *         objects
     */
    public Flux<Quote> postQuotes
        (String route,
         List<Integer> quoteIds) {
        // Use WebUtils to create a URL to the POST_QUOTES endpoint of
        // the 'route' microservice.
        var uri = WebUtils
            .buildUriString(route + "/" + POST_QUOTES);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching quoteIds from the 'route' microservice.
            .makePostRequestFlux(mWebClient,
                                 uri,
                                 quoteIds,
                                 Quote.class);
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries}.
     *
     * @param route The microservice that performs the request
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @return A {@link Flux} that emits {@link Quote} objects
     *         matching the queries
     */
    public Flux<Quote> search
        (String route,
         List<String> queries) {
        // Use WebUtils to create a URL to the POST_SEARCHES
        // endpoint of the 'route' microservice.
        var uri = WebUtils
            .buildUriString(route + "/" + POST_SEARCHES);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching queries from the 'route' microservice.
            .makePostRequestFlux(mWebClient,
                                 uri,
                                 queries,
                                 Quote.class);
    }

    /**
     * Search the Zippy microservice for quotes containing all the
     * given {@link List} of {@code queries} using a custom SQL
     * method.
     *
     * @param route The microservice that performs the request
     * @param queries The {@link List} of {@code queries} to search
     *                for
     * @return A {@link Flux} that emits {@link Quote} objects
     *         matching the queries
     */
    public Flux<Quote> searchEx
        (String route, 
         List<String> queries) {
        // Use WebUtils to create a URL to the POST_SEARCHES_EX
        // endpoint of the 'route' microservice.
        var uri = WebUtils
            .buildUriString(route + "/" + POST_SEARCHES_EX);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching queries from the 'route' microservice.
            .makePostRequestFlux(mWebClient,
                                 uri,
                                 queries,
                                 Quote.class);
    }
}
