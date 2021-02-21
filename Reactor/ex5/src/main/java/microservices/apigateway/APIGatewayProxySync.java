package microservices.apigateway;

import datamodels.AirportInfo;
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
     * The URI that denotes a remote method to find information about
     * all the airports synchronously.
     */
    private final String mFindAirportsURISync =
        "/microservices/APIGatewaySync/_getAirportList";

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
        "/microservices/APIGatewaySync/_findFlights";

    /**
     * Constructor initializes the super class.
     */
    public APIGatewayProxySync() {
        super();
    }

    /**
     * @return A List that contains {@code AirportInfo} objects
     */
    public List<AirportInfo> findAirportInfo() {
        // Send a GET request to the URI template and return the
        // response as an Http ResponseEntity.
        ResponseEntity<AirportInfo[]> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mFindAirportsURISync,
                          AirportInfo[].class);

        // Convert the ResponseEntity to a List of AirportInfo objects
        // and return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous computations.
     *
     * @param currencyConversion The currency to convert from and to
     * @return A {@code TripResponse} that contains the best priced trip
     */
    public TripResponse findBestPrice(TripRequest tripRequest,
                                      CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // GET the given tripRequest to the URI template and return
        // the response as an HTTP ResponseEntity.
        ResponseEntity<TripResponse> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mFindBestPriceURISync,
                           flightRequest,
                           TripResponse.class);

        // Convert the ResponseEntity to a TripResponse and return it.
        return Objects.requireNonNull(responseEntity.getBody());
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * synchronously.

     * @param currencyConversion The currency to convert from and to
     * @return A List containing all the matching {@code TripResponse} objects
     */
    public List<TripResponse> findFlights(TripRequest tripRequest,
                                          CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // GET the given tripRequest to the URI template and return
        // the response as an HTTP ResponseEntity.
        ResponseEntity<TripResponse[]> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mFindFlightsURISync,
                           flightRequest,
                           TripResponse[].class);

        // Convert the ResponseEntity to a List of TripResponses and
        // return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
