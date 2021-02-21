package microservices.airports;

import datamodels.AirportInfo;
import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a proxy to the synchronous AirportList
 * microservice that provides a list of airport codes and associated
 * airport names.
 */
public class AirportListProxySync 
       extends AirportListProxyBase {
    /**
     * The URI that denotes the remote method to obtain the list of
     * airport codes/names synchronously.
     */
    private final String mFindAirportListsURISync =
            "/microservices/AirportListSync/_getAirportList";

    /**
     * Constructor initializes the super class.
     */
    public AirportListProxySync() {
        super();
    }

    /**
     * @return A List that contains {@code AirportInfo} objects
     */
    public List<AirportInfo> findAirportInfo() {
        CurrencyConversion currencyConversion =
            new CurrencyConversion("JFK", "BNA", 1.0);
        TripRequest tripRequest =
            new TripRequest();

        // Send a GET request to the URI template and return the
        // response as an Http ResponseEntity.
        ResponseEntity<AirportInfo[]> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mFindAirportListsURISync,
                          AirportInfo[].class,
                          currencyConversion /*,
                          tripRequest */);

        // Convert the ResponseEntity to a List of AirportInfo objects
        // and return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
