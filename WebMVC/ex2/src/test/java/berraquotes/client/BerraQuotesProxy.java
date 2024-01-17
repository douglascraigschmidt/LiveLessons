package berraquotes.client;

import berraquotes.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import berraquotes.utils.WebUtils;

import java.util.List;

import static berraquotes.common.Constants.EndPoint.*;
import static berraquotes.common.Constants.EndPoint.Params.*;

/**
 * This class is a proxy to the {@code BerraQuotesApplication}
 * microservice and its {@code BerraQuotesController}.
 */
@Component
public class BerraQuotesProxy {
    /**
     * This field connects the {@link BerraQuotesProxy} to the
     * {@link RestTemplate} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

    /**
     * @param strategy The quote checking strategy to use
     * @return An {@link List} containing all {@link
     *         Quote} objects
     */
    public List<Quote> getAllQuotes(int strategy) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            // Create the path for the GET_ALL_QUOTES request.
            .fromPath(GET_ALL_QUOTES)
            // Include the strategy.
            .queryParam(STRATEGY, strategy)
            // Build the URI.
            .build()
            // Convert the URI to a String.
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Pass the encoded URL.
                                uri,
                                // Return type is a Quote array.
                                Quote[].class);
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param strategy The quote checking strategy to use
     * @param quoteIds A {@link List} containing the given
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(int strategy,
                                 List<Integer> quoteIds) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            // Create the path for the GET_QUOTES request.
            .fromPath(GET_QUOTES)
            // Include the strategy.
            .queryParam(STRATEGY, strategy)
            // Create the query param, which encodes the quote ids.
            .queryParam(QUOTE_IDS,
                        WebUtils
                        // Convert List to String.
                        .list2String(quoteIds))
            // Build the URI.
            .build()
            // Convert to a URI String.
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Pass the encoded URL.
                                uri,
                                // Return type is a Quote array.
                                Quote[].class);
    }

    /**
     * Get a {@link List} that contains quotes that match the {@code
     * query}.
     *
     * @param strategy The quote checking strategy to use
     * @param query A {@link String} to search for
     * @return An {@link List} containing matching {@link
     *         Quote} objects
     */
    public List<Quote> searchQuotes(int strategy,
                                    String query) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            // Create a builder initialized with the given URI string
            .fromUriString(GET_SEARCH)
            // Append an encoded query string as a path segment to
            // the URI. The query string is URL-encoded to ensure
            // it is a valid URI component.
            .pathSegment(WebUtils.encodeQuery(query))
            /*
            // Here's an alternative approach.
            .fromPath(GET_SEARCH
                      + "/"
                      + WebUtils.encodeQuery(query))
             */
            // Include the strategy.
            .queryParam(STRATEGY, strategy)
            // Build a UriComponents instance from the various
            // components contained in this builder.
            .build()
            // Convert to a URI String.
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a Quote array.
                                Quote[].class);
    }

    /**
     * Get a {@link List} that contains quotes that match the
     * {@code queries}.
     *
     * @param strategy The quote checking strategy to use
     * @param queries A {@link List} of {@link String} queries to
     *        search for
     * @return An {@link List} containing matching {@link
     *         Quote} objects
     */
    public List<Quote> searchQuotes(int strategy,
                                    List<String> queries) {
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            // Create the path for the GET_SEARCHES request.
            .fromPath(GET_SEARCHES)
            // Include the strategy.
            .queryParam(STRATEGY, strategy)
            // Encode the List of queries into the URI.
            .queryParam(QUERIES, WebUtils
                        .list2String(queries))
            // Build a UriComponents instance from the various
            // components contained in this builder.
            .build()
            // Convert to a URI String.
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a Quote array.
                                Quote[].class);
    }
}
