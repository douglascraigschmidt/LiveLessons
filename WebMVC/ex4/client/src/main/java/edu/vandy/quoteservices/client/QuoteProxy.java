package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_SEARCHES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_ALL_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.Params.QUOTE_IDS_PARAM;
import static edu.vandy.quoteservices.common.Constants.EndPoint.Params.PARALLEL;

/**
 * This class is a proxy to the {@code GatewayApplication} API gateway
 * and its {@code GatewayController}.
 */
@Component
public class QuoteProxy {
    /**
     * This field connects the {@link QuoteProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param service The service that will perform the request
     * @return An {@link List} containing all the {@link Quote}
     *         objects
     */
    public List<Quote> getAllQuotes(String service) {
        // Create the encoded URL.
        var url = UriComponentsBuilder
            // Create the path for the GET_ALL_QUOTES request,
            // including the 'service'.
            .fromPath(service + "/" + GET_ALL_QUOTES)

            // Build the URI.
            .build()

            // Convert the URI to a String.
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Pass the encoded URL.
                                url,
                                // Return type is a Quote array.
                                Quote[].class);
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param service The service that will perform the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(String service,
                                 List<Integer> quoteIds,
                                 Boolean parallel) {
        // Create the encoded URL.
        var uri = UriComponentsBuilder
            // Create the path for the GET_QUOTES request, including
            // the 'service'.
            .fromPath(service + "/" + GET_QUOTES)

            // Create the query param, which encodes the quote ids.
            .queryParam(QUOTE_IDS_PARAM, WebUtils
                        // Convert List to String.
                        .list2String(quoteIds))
            .queryParam(PARALLEL, parallel)

            // Build the URI.
            .build()

            // Convert the URI to a String.
            .toUriString();

        System.out.println("uri = " + uri);

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Pass the encoded URI.
                                uri,
                                // Return type is a Quote array.
                                Quote[].class);
    }

    /**
     * Search for quotes containing the given {@link List} of queries.
     *
     * @param service The service that will perform the request
     * @param queries The {@link List} of queries to search for
     * @param parallel Run the queries in parallel if true, else run sequentially
     * @return A {@link List} of {@link Quote} objects that match the
     *         queries
     */
    List<Quote> searchQuotes(String service,
                             List<String> queries,
                             boolean parallel) {
        // Use the UriComponentsBuilder to create a URL to the
        // "searches" endpoint of the 'quote' microservices.  You'll
        // need to convert the List of queries into a String.

        var uri = UriComponentsBuilder
            .fromPath(service + "/" + GET_SEARCHES)
            .queryParam("queries",
                        WebUtils
                        // Convert the List to a String.
                        .list2String(queries))
            .queryParam("parallel", parallel)
            .build()
            .toUriString();

        // Use WebUtils.makeGetRequestList() and mRestTemplate to get
        // a List of all matching quotes from the 'quotes'
        // microservices.
        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a Movie array.
                                Quote[].class);
    }
}
