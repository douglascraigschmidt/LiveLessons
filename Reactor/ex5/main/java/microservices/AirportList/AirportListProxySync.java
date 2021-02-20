package microservices.AirportList;

import datamodels.AirportInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
        // Send a GET request to the URI template and return the
        // response as an Http ResponseEntity.
        ResponseEntity<AirportInfo[]> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mFindAirportListsURISync,
                          AirportInfo[].class);

        // Convert the ResponseEntity to a List of AirportInfo objects
        // and return it.
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
