package microservices.airlines.SWA;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.airlines.PriceProxySync;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a proxy to the synchronous SWAPrice
 * microservice that provides prices for Southwest Airlines (SWA)
 * flights.

 */
public class SWAPriceProxySync 
       extends SWAPriceProxyBase
       implements PriceProxySync {
    /**
     * The URI that denotes the remote method to query the SWA price
     * database synchronously.
     */
    private final String mFindSWAPricesURISync =
            "/microservices/airlines/SWASync/_getTripPrices";

    /**
     * Constructor initializes the super class.
     */
    public SWAPriceProxySync() {
        super();
    }

    /**
     * Returns a List that contains {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param tripRequest The trip to price
     * @return A List that contains {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    @Override
    public List<TripResponse> findTrips(TripRequest tripRequest) {
        // POST the given tripRequest to the URI template and return
        // the response as an Http ResponseEntity.
        ResponseEntity<TripResponse[]> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mFindSWAPricesURISync,
                           tripRequest,
                           TripResponse[].class);

        // Convert the ResponseEntity to a List of TripResponses and
        // return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
