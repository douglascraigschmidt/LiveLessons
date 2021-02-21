package clients;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.apigateway.APIGatewayProxySync;
import utils.Options;

import java.util.List;

/**
 * A Java utility class containing a client that uses Java
 * object-oriented features and Spring MVC to synchronously invoke
 * microservices that provide various flight-related services for the
 * Airline Booking App (ABA).
 */
public class COOPClient {
    /**
     * A proxy that's used to communicate with the APIGateway
     * microservice.
     */
    private static final APIGatewayProxySync sAPIGatewayProxySync =
        new APIGatewayProxySync();

    /**
     * This test invokes microservices to synchronously determine the
     * best price for a {@code trip} using the given {@code
     * currencyConversion}.
     */
    public static void runSyncTests(TripRequest trip,
                                    CurrencyConversion currencyConversion) {
        System.out.println("begin runSyncTests()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++) {
            // Find and display all the flight prices synchronously.
            findFlightsSync(i + 1,
                            trip, currencyConversion);

            // Find and display just the best flight price
            // synchronously.
            findBestPriceSync(i + 1,
                              trip,
                              currencyConversion);
        }

        System.out.println("end runSyncTests()");
    }

    /**
     * Displays all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous
     * computations/communications.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current desired trip
     * @param currencyConversion The currency to convert from and to
     */
    private static void findFlightsSync(int iteration,
                                        TripRequest tripRequest,
                                        CurrencyConversion currencyConversion) {
        List<TripResponse> trips = sAPIGatewayProxySync
            .findFlights(tripRequest,
                         currencyConversion);

        trips.forEach(tripResponse ->
                      Options.print("Iteration #"
                                    + iteration
                                    + " The price is: "
                                    + tripResponse.getPrice()
                                    + " GBP on "
                                    + tripResponse.getAirlineCode()));
    }

    /**
     * Displays the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous
     * computations/communication.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current trip being priced.
     */
    private static void findBestPriceSync(int iteration,
                                          TripRequest tripRequest,
                                          CurrencyConversion currencyConversion) {
        TripResponse tripResponse = sAPIGatewayProxySync
            .findBestPrice(tripRequest,
                           currencyConversion);

        Options.print("Iteration #"
                      + iteration
                      + " The best price is: "
                      + tripResponse.getPrice()
                      + " GBP on "
                      + tripResponse.getAirlineCode());
    }
}
