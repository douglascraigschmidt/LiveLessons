package microservices.apigateway.controller;

import datamodels.AirportInfo;
import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.airports.AirportListProxySync;
import microservices.apigateway.FlightRequest;
import microservices.exchangerate.ExchangeRateProxySync;
import microservices.flightprice.FlightPriceProxySync;
import org.springframework.web.bind.annotation.*;
import utils.ExceptionUtils;
import utils.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST requests via object-oriented programming.  These
 * requests are mapped to method(s) that provide the external entry
 * point into the ABA microservices synchronously.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@code APIGatewaySync}.  POST requests
 * invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/APIGatewaySync")
public class APIGatewayControllerSync {
    /**
     * A sync proxy to the FlightPrice microservice.
     */
    private final FlightPriceProxySync mFlightPriceProxySync =
        new FlightPriceProxySync();

    /**
     * A sync proxy to the ExchangeRate microservice.
     */
    private final ExchangeRateProxySync mExchangeRateProxySync =
        new ExchangeRateProxySync();

    /**
     * A sync proxy to the AirportList microservice
     */
    private final AirportListProxySync mAirportListProxy =
        new AirportListProxySync();

    /**
     * This method finds information about all the airports
     * synchronously.
     *
     * WebFlux maps HTTP GET requests sent to the /_getAirportList
     * endpoint to this method.
     *
     * @return A List that contains all {@code AirportInfo} objects
     */
    @GetMapping("_getAirportList")
    public List<AirportInfo> getAirportInfo() {
        return mAirportListProxy.findAirportInfo();
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous computations.
     *
     * WebFlux maps HTTP POST requests sent to the /_findBestPrice
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A {@code TripResponse} that contains the best price for the desired trip
     */
    @PostMapping("_findBestPrice")
    public TripResponse findBestPrice(@RequestBody FlightRequest flightRequest) {
        return this
                .makeBestPrice()
                .compute(flightRequest.tripRequest,
                        flightRequest.currencyConversion);
    }

    /**
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous
     * computations/communications.
     *
     * WebFlux maps HTTP POST requests sent to the /_findFlights
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A List that contains all the matching {@code TripResponse} objects
     */
    @PostMapping("_findFlights")
    public List<TripResponse> findFlights(@RequestBody FlightRequest flightRequest) {
        return this
            .makeAllPrices()
            .compute(flightRequest.tripRequest,
                     flightRequest.currencyConversion);
    }

    /**
     * This functional interface forms the basis of the tasks that
     * compute the best price for a given flight.
     */
    @FunctionalInterface
    interface BestFlightPrice {
        /**
         * Computes the converted best price for all flights matching
         * a given {@code tripRequest}
         *
         * @param tripRequest        The given trip request
         * @param currencyConversion The currency to convert to and from
         * @return A {@code TripResponse} that has the best price for
         * the {@code tripRequest}
         */
        TripResponse compute(TripRequest tripRequest,
                             CurrencyConversion currencyConversion);
    }

    /**
     *
     */
    private final Map<String, BestFlightPrice> sBestPriceMap =
            new HashMap<>() {
                {
                    put("sequential", makeSequentialBestPrice());
                    put("threads", makeThreadsBestPrice());
                    put("completionService", makeCompletionServiceBestPrice());
                    put("executorService", makeExecutorServiceBestPrice());
                }
            };

    /**
     * @return A task that computes the best flight price for a given
     * {@code tripRequest} sequentially.
     */
    private BestFlightPrice makeSequentialBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse tripResponse = mFlightPriceProxySync
                    // Synchronously find the best price for the tripRequest.
                    .findBestPrice(tripRequest,
                            Options.instance().maxTimeout());
            Double rate = mExchangeRateProxySync
                    // Synchronously determine the exchange rate.
                    .queryForExchangeRate(currencyConversion);

            return tripResponse.convert(rate);
        };
    }

    /**
     * @return A task that uses Java Threads to compute the best
     * flight price for a given {@code tripRequest} sequentially.
     */
    private BestFlightPrice makeThreadsBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse[] trip = new TripResponse[1];
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<>() {
                {
                    trip[0] = mFlightPriceProxySync
                            // Synchronously find the best price for the tripRequest.
                            .findBestPrice(tripRequest,
                                    Options.instance().maxTimeout());
                }

                {
                    rate[0] = mExchangeRateProxySync
                            // Synchronously determine the exchange rate.
                            .queryForExchangeRate(currencyConversion);
                }
            };

            // Start all the threads.
            threads.forEach(Thread::start);

            // Wait for all the threads to complete.
            threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            // When the calls to compute the trip and rate complete
            // convert the price.
            return trip[0].convert(rate[0]);
        };
    }

    /**
     * @return A task that uses the Java CompletionService to compute
     * the best flight price for a given {@code tripRequest}
     * sequentially.
     */
    private BestFlightPrice makeCompletionServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                        new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> mFlightPriceProxySync
                        // Synchronously find the best price for the tripRequest.
                        .findBestPrice(tripRequest,
                                Options.instance().maxTimeout());

                Callable<Object> c2 = () -> mExchangeRateProxySync
                        // Synchronously determine the exchange rate.
                        .queryForExchangeRate(currencyConversion);

                cs.submit(c1);
                cs.submit(c2);

                TripResponse tripResponse = null;
                Double rate = null;

                for (int i = 0; i < 2; i++) {
                    Object o = cs.take().get();
                    if (o instanceof TripResponse)
                        tripResponse = (TripResponse) o;
                    else if (o instanceof Double)
                        rate = (Double) o;
                }
                assert tripResponse != null;
                assert rate != null;

                // When the calls to compute the trip and rate complete
                // convert the price.

                return tripResponse.convert(rate);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * @return A task that uses the Java ExecutorService to compute
     * the best flight price for a given {@code tripRequest}
     * sequentially.
     */
    private BestFlightPrice makeExecutorServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find the best price for the tripRequest.
                Callable<TripResponse> c1 = () -> mFlightPriceProxySync
                        .findBestPrice(tripRequest,
                                Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> mExchangeRateProxySync
                        .queryForExchangeRate(currencyConversion);

                // Create a new cached thread pool.
                ExecutorService es = Executors.newCachedThreadPool();

                // Compute the best price and the exchange rate
                // concurrently.
                Future<TripResponse> f1 = es.submit(c1);
                Future<Double> f2 = es.submit(c2);

                // Block until the responses are done.
                TripResponse tripResponse = f1.get();
                Double rate = f2.get();

                // When the calls to compute the trip and rate complete
                // convert the price.
                return tripResponse.convert(rate);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * This functional interface forms the basis of the tasks that
     * compute the prices for all the flights.
     */
    @FunctionalInterface
    interface AllFlightPrices {
        /**
         * Computes the converted prices for all flights matching a
         * given {@code tripRequest}
         *
         * @param tripRequest        The given trip request
         * @param currencyConversion The currency to convert to and from
         * @return A List of {@code TripResponse} objects that match
         * the {@code tripRequest}
         */
        List<TripResponse> compute(TripRequest tripRequest,
                                   CurrencyConversion currencyConversion);
    }

    /**
     * Create a Map that associates the names of the tasks with
     * implementations that finds the prices of all the flights.
     */
    private final Map<String, AllFlightPrices> mAllPricesMap =
            new HashMap<>() {
                {
                    put("sequential", makeSequentialAllPrices());
                    put("threads", makeThreadsAllPrices());
                    put("completionService", makeCompletionServiceAllPrices());
                    put("executorService", makeExecutorServiceAllPrices());
                }
            };

    /**
     * @return A task that computes all the flight prices
     * sequentially.
     */
    private AllFlightPrices makeSequentialAllPrices() {
        return (tripRequest, currencyConversion) -> {
            List<TripResponse> trips = mFlightPriceProxySync
                    // Synchronously find all the flights for the
                    // tripRequest.
                    .findFlights(tripRequest,
                            Options.instance().maxTimeout());
            Double rate = mExchangeRateProxySync
                    // Synchronously determine the exchange rate.
                    .queryForExchangeRate(currencyConversion);

            // Convert all the trip prices using the exchange rate.
            return convertTripPrices(trips, rate);
        };
    }

    /**
     * @return A task that uses the Java Threads to compute all the
     * flight prices concurrently.
     */
    private AllFlightPrices makeThreadsAllPrices() {
        return (tripRequest, currencyConversion) -> {
            List<TripResponse> trips = new ArrayList<>();
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<>() {
                {
                    // Synchronously find all the flights
                    // for the tripRequest.
                    trips.addAll(mFlightPriceProxySync
                            .findFlights(tripRequest, Options.instance().maxTimeout()));
                }

                {
                    rate[0] = mExchangeRateProxySync
                            // Synchronously determine the exchange rate.
                            .queryForExchangeRate(currencyConversion);
                }
            };

            // Start all the threads.
            threads.forEach(Thread::start);

            // Wait for all the threads to complete.
            threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            // Convert all the trip prices using the exchange rate.
            return convertTripPrices(trips, rate[0]);
        };
    }

    /**
     * @return A task that uses the Java CompletionService to compute
     * all the flight prices concurrently.
     */
    private AllFlightPrices makeCompletionServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                        new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> mFlightPriceProxySync
                        // Synchronously find all the flights for
                        // the tripRequest.
                        .findFlights(tripRequest,
                                Options.instance().maxTimeout());

                Callable<Object> c2 = () -> mExchangeRateProxySync
                        // Synchronously determine the exchange rate.
                        .queryForExchangeRate(currencyConversion);

                cs.submit(c1);
                cs.submit(c2);

                List<TripResponse> trips = null;
                Double rate = null;

                for (int i = 0; i < 2; i++) {
                    Object o = cs.take().get();
                    if (o instanceof List<?>) {
                        //noinspection unchecked
                        trips = (List<TripResponse>) o;
                    } else if (o instanceof Double)
                        rate = (Double) o;
                }
                assert trips != null;
                assert rate != null;

                // Convert all the trip prices using the exchange rate.
                return convertTripPrices(trips, rate);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * @return A task that uses the Java ExecutorService to compute
     * all the flight prices concurrently.
     */
    private AllFlightPrices makeExecutorServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find all the flights for the tripRequest.
                Callable<List<TripResponse>> c1 = () -> mFlightPriceProxySync
                        .findFlights(tripRequest,
                                Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> mExchangeRateProxySync
                        .queryForExchangeRate(currencyConversion);

                // Create a new cached thread pool.
                ExecutorService es = Executors.newCachedThreadPool();

                // Compute the best price and the exchange rate
                // concurrently.
                Future<List<TripResponse>> f1 = es.submit(c1);
                Future<Double> f2 = es.submit(c2);

                // Block until the responses are done.
                List<TripResponse> trips = f1.get();
                Double rate = f2.get();

                // Convert all the trip prices using the exchange rate.
                return convertTripPrices(trips, rate);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * @return The task to use to compute the best flight price.
     */
    private BestFlightPrice makeBestPrice() {
        return sBestPriceMap.get(Options.instance().getSyncTask());
    }

    /**
     * @return The task to use to compute all the flight prices.
     */
    private AllFlightPrices makeAllPrices() {
        return mAllPricesMap.get(Options.instance().getSyncTask());
    }

    /**
     * Converts all the prices in {@code trips} using the exchange {@code rate}.
     *
     * @param trips The list of trips to convert
     * @param rate  The exchange rate used for the conversion
     * @return An updated list of trips that have been converted by the exchange rate
     */
    private List<TripResponse> convertTripPrices(List<TripResponse> trips,
                                                        Double rate) {
        trips.forEach(trip -> trip.convert(rate));
        return trips;
    }
}
