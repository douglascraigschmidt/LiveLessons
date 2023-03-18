package microservices.airlines.AA;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.airlines.PriceProxySync;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a proxy to the synchronous AAPrice
 * microservice that provides prices for Southwest Airlines (AA)
 * flights.
 */
public class AAPriceProxySync 
       extends AAPriceProxyBase
       implements PriceProxySync {
    /**
     * The URI that denotes the remote method to query the AA price
     * database synchronously.
     */
    private final String mFindAAPricesURISync =
            "/microservices/airlines/AASync/_getTripPrices";

    /**
     * Constructor initializes the super class.
     */
    public AAPriceProxySync() {
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
            .postForEntity(mSERVER_BASE_URL + mFindAAPricesURISync,
                           tripRequest,
                           TripResponse[].class);

        // Convert the ResponseEntity to a List of TripResponses and
        // return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
