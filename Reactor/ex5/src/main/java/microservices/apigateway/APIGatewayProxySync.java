package microservices.apigateway;

import datamodels.CurrencyConversion;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a proxy to the synchronous APIGateway
 * microservice that returns the current exchange that converts one
 * currency to another.
 */
public class APIGatewayProxySync
       extends APIGatewayProxyBase {
    /**
     * The URI that denotes a remote method to find the best flight
     * price synchronously.
     */
    private final String mFindBestPriceURISync =
        "/microservices/APIGatewaySync/_findBestPrice";

    /**
     * The URI that denotes a remote method to find all flights
     * synchronously.
     */
    private final String mFindFlightsURISync =
        "/microservices/APIGateway/_findFlights";

    /**
     * Constructor initializes the super class.
     */
    public APIGatewayProxySync() {
        super();
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous computations.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param currencyConversion The currency to convert from and to
     * @return A {@code TripResponse} that contains the best priced trip
     */
    public TripResponse findBestPrice(Scheduler scheduler,
                                      TripRequest tripRequest,
                                      CurrencyConversion currencyConversion) {
        // GET the given tripRequest to the URI template and return
        // the response as an HTTP ResponseEntity.
        ResponseEntity<TripResponse> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mFindBestPriceURISync,
                          TripResponse.class,
                          tripRequest,
                          currencyConversion);

        // Convert the ResponseEntity to a TripResponse and return it.
        return Objects.requireNonNull(responseEntity.getBody());
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * synchronously.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param currencyConversion The currency to convert from and to
     * @return A List containing all the matching {@code TripResponse} objects
     */
    public List<TripResponse> findFlights(Scheduler scheduler,
                                          TripRequest tripRequest,
                                          CurrencyConversion currencyConversion) {
        // GET the given tripRequest to the URI template and return
        // the response as an HTTP ResponseEntity.
        ResponseEntity<TripResponse[]> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mFindFlightsURISync,
                           TripResponse[].class,
                           tripRequest,
                           currencyConversion);

        // Convert the ResponseEntity to a List of TripResponses and
        // return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
