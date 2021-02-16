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
 * This class serves as a proxy to the AirportList microservice, which
 * provides a list of airport codes and associated airport names.
 */
public class AirportListProxy {
    /**
     * A synchronous client used to perform HTTP requests via simple
     * template method API over underlying HTTP client libraries
     */
    private final RestTemplate mRestTemplate = new RestTemplate();

    /**
     * The URI that denotes the remote method to obtain the list of
     * airport codes/names asynchronously.
     */
    private final String mFindAirportListsURIAsync =
            "/microservices/AirportListAsync/_getAirportList";

    /**
     * The URI that denotes the remote method to obtain the list of
     * airport codes/names synchronously.
     */
    private final String mFindAirportListsURISync =
            "/microservices/AirportListSync/_getAirportList";

    /**
     * Host/port where the server resides.
     */
    private final String mSERVER_BASE_URL =
            "http://localhost:8085";

    /**
     * The WebClient provides the means to access the AirportList
     * microservice.
     */
    private WebClient mAirportLists = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(mSERVER_BASE_URL)

        // Build the webclient.
        .build();

    /**
     * Constructor initializes the fields.
     */
    public AirportListProxy() {
    }

    /**
     * Returns a Flux that emits {@code AirportInfo} objects.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @return A Flux that emits {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    public Flux<AirportInfo> findAirportInfoAsync(Scheduler scheduler) {
        return Mono
            // Return a Flux containing the list of airport
            // information.
            .fromCallable(() -> mAirportLists
                          // Create an HTTP POST request.
                          .get()

                          // Add the uri to the baseUrl.
                          .uri(mFindAirportListsURIAsync)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Flux of AirportInfo
                          // objects.
                          .bodyToFlux(AirportInfo.class))

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<AirportInfo>.
            .flatMapMany(Function.identity());
    }

    /**
     * @return A List that contains {@code AirportInfo} objects
     */
    public List<AirportInfo> findAirportInfoSync() {
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
