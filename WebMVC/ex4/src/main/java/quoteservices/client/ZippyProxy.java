package quoteservices.client;

import quoteservices.common.ZippyQuote;
import quoteservices.server.zippy.ZippyApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import quoteservices.server.zippy.ZippyController;
import quoteservices.utils.WebUtils;

import java.util.List;

import static quoteservices.common.Components.getRestTemplate;
import static quoteservices.common.Constants.EndPoint.GET_ZIPPY_QUOTE;
import static quoteservices.common.Constants.ZIPPY_MICROSERVICE_BASE_URL;

/**
 * This class is a proxy to the {@link ZippyApplication} microservice
 * and the {@link ZippyController}.
 */
@Component
public class ZippyProxy {
    /**
     * This field connects the {@link ZippyProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    private final RestTemplate mRestTemplate =
        getRestTemplate(ZIPPY_MICROSERVICE_BASE_URL);

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         ZippyQuote} objects
     */
    public List<ZippyQuote> getZippyQuotes
        (List<Integer> quoteIds) {
        return WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                // Create the encoded URL.
                                UriComponentsBuilder
                                .fromPath(GET_ZIPPY_QUOTE)
                                .queryParam("quoteIds",
                                            WebUtils
                                            // Convert List to String.
                                            .list2String(quoteIds))
                                .build()
                                .toString(),
                                // Return type is a ZippyQuote array.
                                ZippyQuote[].class);
    }
}

