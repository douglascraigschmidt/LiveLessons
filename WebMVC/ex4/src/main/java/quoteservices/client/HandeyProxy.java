package quoteservices.client;

import quoteservices.common.HandeyQuote;
import quoteservices.server.handey.HandeyApplication;
import quoteservices.server.handey.HandeyController;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import quoteservices.utils.WebUtils;

import java.util.List;

import static quoteservices.common.Components.getRestTemplate;
import static quoteservices.common.Constants.EndPoint.GET_HANDEY_QUOTE;
import static quoteservices.common.Constants.HANDEY_MICROSERVICE_BASE_URL;

/**
 * This class is a proxy to the {@link HandeyApplication} service
 * and the {@link HandeyController}.
 */
@Component
public class HandeyProxy {
    /**
     * This field connects the {@link HandeyProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    private final RestTemplate mRestTemplate =
        getRestTemplate(HANDEY_MICROSERVICE_BASE_URL);

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         HandeyQuote} objects
     */
    public List<HandeyQuote> getHandleQuotes(List<Integer> quoteIds) {
        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Create the encoded URL.
                                UriComponentsBuilder
                                .fromPath(GET_HANDEY_QUOTE)
                                .queryParam("quoteIds",
                                            WebUtils
                                            // Convert List to String.
                                            .list2String(quoteIds))
                                .build()
                                .toString(),
                                // Return type is a HandeyQuote array.
                                HandeyQuote[].class);
    }
}

