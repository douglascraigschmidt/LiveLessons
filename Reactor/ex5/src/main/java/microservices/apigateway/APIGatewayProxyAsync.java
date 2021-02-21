package microservices.apigateway;

import datamodels.CurrencyConversion;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import utils.Options;

import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous APIGateway
 * microservice.
 */
public class APIGatewayProxyAsync
       extends APIGatewayProxyBase {
    /**
     * The URI that denotes a remote method to find the best flight
     * price asynchronously.
     */
    private final String mFindBestPriceURIAsync =
        "/microservices/APIGatewayAsync/_findBestPrice";

    /**
     * The URI that denotes a remote method to find all flights
     * asynchronously.
     */
    private final String mFindFlightsURIAsync =
        "/microservices/APIGatewayAsync/_findFlights";

    /**
     * Constructor initializes the super class.
     */
    public APIGatewayProxyAsync() {
        super();
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param tripRequest The desired trip to take
     * @param currencyConversion The currency to convert from and to
     * @return A Mono that emits the best priced {@code TripResponse}
     */
    public Mono<TripResponse> findBestPrice(Scheduler scheduler,
                                            TripRequest tripRequest,
                                            CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // Return a Mono that emits the best priced TripResponse.
        return Mono
            .fromCallable(() -> mAPIGateway
                          // Create an HTTP POST request.
                          .post()

                          // Update the uri and add it to the baseUrl.
                          .uri(mFindBestPriceURIAsync)

                          // Encode the flightRequest in the body of
                          // the request.
                          .bodyValue(flightRequest)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Mono of TripResponse.
                          .bodyToMono(TripResponse.class))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<TripResponse>.
            .flatMap(Function.identity());
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The desired trip to take
     * @param currencyConversion The currency to convert from and to
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    public Flux<TripResponse> findFlights(Scheduler scheduler,
                                          TripRequest tripRequest,
                                          CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // Return a Flux that emits all flights matching tripRequest.
        return Mono
            .fromCallable(() -> mAPIGateway
                          // Create an HTTP POST request.
                          .post()

                          // Update the uri and add it to the baseUrl.
                          .uri(mFindFlightsURIAsync)

                          // Encode the flightRequest in the body of
                          // the request.
                          .bodyValue(flightRequest)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Flux of TripResponse.
                          .bodyToFlux(TripResponse.class))

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<TripResponse>.
            .flatMapMany(Function.identity());
    }
}
