package microservices.flightprice;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a proxy to the synchronous FlightPrice
 * microservice.
 */
public class FlightPriceProxySync
       extends FlightPriceProxyBase {
    /**
     * The URI that denotes the remote method to find the best price
     * for a trip synchronously.
     */
    private final String mFindBestPriceURISync =
            "/microservices/flightPriceSync/_findBestPrice";

    /**
     * The URI that denotes the remote method to find all the matching
     * flights for a trip.
     */
    private final String mFindFlightsURISync =
            "/microservices/flightPriceSync/_findFlights";

    /**
     * Constructor initializes the super class.
     */
    public FlightPriceProxySync() {
        super();
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * synchronously.
     *
     * @param tripRequest The desired trip 
     * @param maxTime Max time to wait before throwing TimeoutException
     * @return A List that contains all the matching {@code TripResponse} objects
     */
    public List<TripResponse> findFlights(TripRequest tripRequest,
                                          Duration maxTime) {
        // POST the given tripRequest to the URI template and return
        // the response as an Http ResponseEntity.
        ResponseEntity<TripResponse[]> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mFindFlightsURISync,
                           tripRequest,
                           TripResponse[].class);

        // Convert the ResponseEntity to a List of TripResponses and
        // return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }

    /**
     * Finds the best price for the {@code tripRequest} synchronously.
     *
     * @param tripRequest The trip to price
     * @param maxTime Max time to wait before throwing TimeoutException
     * @return A TripResponse containing the best price
     */
    public TripResponse findBestPrice(TripRequest tripRequest,
                                      Duration maxTime) {
        // POST the given tripRequest to the URI template and return
        // the response as an Http ResponseEntity.
        ResponseEntity<TripResponse> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mFindBestPriceURISync,
                           tripRequest,
                           TripResponse.class);

        // Convert the ResponseEntity to a TripResponse and return it.
        return Objects.requireNonNull(responseEntity.getBody());
    }
}
