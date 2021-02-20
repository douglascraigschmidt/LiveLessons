package clients;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.exchangeRate.ExchangeRateProxySync;
import microservices.flightPrice.FlightPriceProxySync;
import utils.ExceptionUtils;
import utils.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 */
public class COOPClient {
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
        List<TripResponse> trips = COOPClient
            .makeAllPrices()
            .compute(tripRequest,
                     currencyConversion);

        trips
            .forEach(tripResponse -> Options
                     .print("Iteration #"
                            + iteration
                            + " The price is: "
                            + tripResponse.getPrice()
                            + " "
                            + currencyConversion.getTo()
                            + " on "
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

        TripResponse tripResponse = COOPClient
            .makeBestPrice()
            .compute(tripRequest,
                     currencyConversion);

        // Print the results.
        Options.print("Iteration #"
                      + iteration
                      + " The best price is: "
                      + tripResponse.getPrice()
                      + " "
                      + currencyConversion.getTo()
                      + " on "
                      + tripResponse.getAirlineCode());
    }

    /**
     * A proxy that's used to communicate with the FlightPrice
     * microservice.
     */
    private static final FlightPriceProxySync sFlightPriceProxySync =
        new FlightPriceProxySync();

    /**
     * A proxy that's used to communicate with the ExchangeRate
     * microservice synchronously.
     */
    private static final ExchangeRateProxySync sExchangeRateProxySync =
            new ExchangeRateProxySync();

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
         * @param tripRequest The given trip request
         * @param currencyConversion The currency to convert to and from
         * @return A {@code TripResponse} that has the best price for
         *         the {@code tripRequest} 
         */
        TripResponse compute(TripRequest tripRequest,
                             CurrencyConversion currencyConversion);
    }

    /**
     *
     */
    private static final Map<String, BestFlightPrice> sBestPriceMap =
        new HashMap<String, BestFlightPrice>() { {
            put("sequential", makeSequentialBestPrice());
            put("threads", makeThreadsBestPrice());
            put("completionService", makeCompletionServiceBestPrice());
            put("executorService", makeExecutorServiceBestPrice());
        } };

    /**
     * @return A task that computes the best flight price for a given
     * {@code tripRequest} sequentially.
     */
    private static BestFlightPrice makeSequentialBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse tripResponse = sFlightPriceProxySync
                // Synchronously find the best price for the tripRequest.
                .findBestPrice(tripRequest,
                               Options.instance().maxTimeout());
            Double rate = sExchangeRateProxySync
                // Synchronously determine the exchange rate.
                .queryForExchangeRate(currencyConversion);

            return tripResponse.convert(rate);
        };
    }

    /**
     * @return A task that uses Java Threads to compute the best
     * flight price for a given {@code tripRequest} sequentially.
     */
    private static BestFlightPrice makeThreadsBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse[] trip = new TripResponse[1];
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<Thread>() {
                    {
                        trip[0] = sFlightPriceProxySync
                        // Synchronously find the best price for the tripRequest.
                        .findBestPrice(tripRequest,
                                       Options.instance().maxTimeout());
                    }

                    {
                        rate[0] = sExchangeRateProxySync
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
    private static BestFlightPrice makeCompletionServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                    new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> sFlightPriceProxySync
                    // Synchronously find the best price for the tripRequest.
                    .findBestPrice(tripRequest,
                                   Options.instance().maxTimeout());

                Callable<Object> c2 = () -> sExchangeRateProxySync
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
    private static BestFlightPrice makeExecutorServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find the best price for the tripRequest.
                Callable<TripResponse> c1 = () -> sFlightPriceProxySync
                    .findBestPrice(tripRequest,
                                   Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> sExchangeRateProxySync
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
         * @param tripRequest The given trip request
         * @param currencyConversion The currency to convert to and from
         * @return A List of {@code TripResponse} objects that match
         *         the {@code tripRequest} 
         */
        List<TripResponse> compute(TripRequest tripRequest,
                                   CurrencyConversion currencyConversion);
    }

    /**
     * Create a Map that associates the names of the tasks with
     * implementations that finds the prices of all the flights.
     */
    private static final Map<String, AllFlightPrices> sAllPricesMap =
        new HashMap<String, AllFlightPrices>() { {
            put("sequential", makeSequentialAllPrices());
            put("threads", makeThreadsAllPrices());
            put("completionService", makeCompletionServiceAllPrices());
            put("executorService", makeExecutorServiceAllPrices());
        } };

    /**
     * @return A task that computes all the flight prices
     * sequentially.
     */
    private static AllFlightPrices makeSequentialAllPrices() {
        return (tripRequest, currencyConversion) -> {
            List<TripResponse> trips = sFlightPriceProxySync
                // Synchronously find all the flights for the
                // tripRequest.
                .findFlights(tripRequest,
                             Options.instance().maxTimeout());
            Double rate = sExchangeRateProxySync
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
    private static AllFlightPrices makeThreadsAllPrices() {
        return (tripRequest, currencyConversion) -> {
            @SuppressWarnings("rawtypes")
            List[] trips = new List[1];
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<Thread>() {
                    {
                        trips[0] = sFlightPriceProxySync
                        // Synchronously find all the flights
                        // for the tripRequest.
                        .findFlights(tripRequest,
                                     Options.instance().maxTimeout());
                    }

                    {
                        rate[0] = sExchangeRateProxySync
                        // Synchronously determine the exchange rate.
                        .queryForExchangeRate(currencyConversion);
                    }
                };

            // Start all the threads.
            threads.forEach(Thread::start);

            // Wait for all the threads to complete.
            threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            // Convert all the trip prices using the exchange rate.
            return convertTripPrices(trips[0], rate[0]);
        };
    }

    /**
     * @return A task that uses the Java CompletionService to compute
     * all the flight prices concurrently.
     */
    private static AllFlightPrices makeCompletionServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                    new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> sFlightPriceProxySync
                    // Synchronously find all the flights for
                    // the tripRequest.
                    .findFlights(tripRequest,
                                 Options.instance().maxTimeout());

                Callable<Object> c2 = () -> sExchangeRateProxySync
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
    private static AllFlightPrices makeExecutorServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find all the flights for the tripRequest.
                Callable<List<TripResponse>> c1 = () -> sFlightPriceProxySync
                    .findFlights(tripRequest,
                                 Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> sExchangeRateProxySync
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
    private static BestFlightPrice makeBestPrice() {
        return sBestPriceMap.get(Options.instance().getSyncTask());
    }

    /**
     * @return The task to use to compute all the flight prices.
     */
    private static AllFlightPrices makeAllPrices() {
        return sAllPricesMap.get(Options.instance().getSyncTask());
    }

    /**
     * Converts all the prices in {@code trips} using the exchange {@code rate}.
     *
     * @param trips The list of trips to convert
     * @param rate The exchange rate used for the conversion
     * @return An updated list of trips that have been converted by the exchange rate
     */
    private static List<TripResponse> convertTripPrices(List<TripResponse> trips, 
                                                        Double rate) {
        trips.forEach(trip -> trip.convert(rate));
        return trips;
    }
}
