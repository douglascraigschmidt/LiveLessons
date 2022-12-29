package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import edu.vandy.quoteservices.utils.WebUtils;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_ALL_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.Params.QUOTE_IDS_PARAM;

/**
 * This class is a proxy to the {@code GatewayApplication} service
 * and the {@code GatewayController}.
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
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(String service,
                                 List<Integer> quoteIds) {
        // Create the encoded URL.
        var url = UriComponentsBuilder
            // Create the path for the GET_QUOTES request, including
            // the 'service'.
            .fromPath(service + "/" + GET_QUOTES)

            // Create the query param, which encodes the quote ids.
            .queryParam(QUOTE_IDS_PARAM,
                        WebUtils
                        // Convert List to String.
                        .list2String(quoteIds))

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
}
