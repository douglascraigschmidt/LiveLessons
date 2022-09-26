package microservices.apigateway;

import datamodels.CurrencyConversion;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import utils.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * This super class factors out code that's common to the
 * APIGatewayProxyAsync and APIGatewayProxySync subclasses.
 */
public class APIGatewayProxyBase {
    /**
     * A synchronous client used to perform HTTP requests via simple
     * template method API over underlying HTTP client libraries
     */
    final RestTemplate mRestTemplate = new RestTemplate();

    /**
     * The WebClient provides the means to access the APIGateway
     * microservice.
     */
    final WebClient mAPIGateway;

    /**
     * Host/port where the server resides.
     */
    final String mSERVER_BASE_URL =
        "http://localhost:8086";

    /**
     * Constructor initializes the fields.
     */
    public APIGatewayProxyBase() {
        mAPIGateway = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }
}
