package tests;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.exchangeRate.ExchangeRateProxy;
import microservices.flightPrice.FlightPriceProxy;
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
public class COOPTests {
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
        List<TripResponse> trips = COOPTests
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

        TripResponse tripResponse = COOPTests
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
    private static final FlightPriceProxy sFlightPriceProxy =
        new FlightPriceProxy();

    /**
     * A proxy that's used to communicate with the ExchangeRate
     * microservice.
     */
    private static final ExchangeRateProxy sExchangeRateProxy =
        new ExchangeRateProxy();

    /**
     *
     */
    @FunctionalInterface
    interface BestPrice {
        TripResponse compute(TripRequest tripRequest,
                             CurrencyConversion currencyConversion);
    }

    /**
     *
     */
    private static final Map<String, BestPrice> sBestPriceMap =
        new HashMap<String, BestPrice>() { {
            put("sequential", makeSequentialBestPrice());
            put("threads", makeThreadsBestPrice());
            put("completionService", makeCompletionServiceBestPrice());
            put("executorService", makeExecutorServiceBestPrice());
        } };

    /**
     *
     * @return
     */
    private static BestPrice makeSequentialBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse tripResponse = sFlightPriceProxy
                // Synchronously find the best price for the tripRequest.
                .findBestPriceSync(tripRequest,
                                   Options.instance().maxTimeout());
            Double rate = sExchangeRateProxy
                // Synchronously determine the exchange rate.
                .queryExchangeRateForSync(currencyConversion);

            return tripResponse.convert(rate);
        };
    }

    /**
     *
     * @return
     */
    private static BestPrice makeThreadsBestPrice() {
        return (tripRequest, currencyConversion) -> {
            TripResponse[] trip = new TripResponse[1];
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<Thread>() {
                    {
                        trip[0] = sFlightPriceProxy
                        // Synchronously find the best price for the tripRequest.
                        .findBestPriceSync(tripRequest,
                                           Options.instance().maxTimeout());
                    }

                    {
                        rate[0] = sExchangeRateProxy
                        // Synchronously determine the exchange rate.
                        .queryExchangeRateForSync(currencyConversion);
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
     *
     * @return
     */
    private static BestPrice makeCompletionServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                    new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> sFlightPriceProxy
                    // Synchronously find the best price for the tripRequest.
                    .findBestPriceSync(tripRequest,
                                       Options.instance().maxTimeout());

                Callable<Object> c2 = () -> sExchangeRateProxy
                    // Synchronously determine the exchange rate.
                    .queryExchangeRateForSync(currencyConversion);

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
     *
     * @return
     */
    private static BestPrice makeExecutorServiceBestPrice() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find the best price for the tripRequest.
                Callable<TripResponse> c1 = () -> sFlightPriceProxy
                    .findBestPriceSync(tripRequest,
                                       Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> sExchangeRateProxy
                    .queryExchangeRateForSync(currencyConversion);

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
     *
     */
    @FunctionalInterface
    interface AllPrices {
        List<TripResponse> compute(TripRequest tripRequest,
                                   CurrencyConversion currencyConversion);
    }

    /**
     *
     */
    private static final Map<String, AllPrices> sAllPricesMap =
        new HashMap<String, AllPrices>() { {
            put("sequential", makeSequentialAllPrices());
            put("threads", makeThreadsAllPrices());
            put("completionService", makeCompletionServiceAllPrices());
            put("executorService", makeExecutorServiceAllPrices());
        }
        };

    /**
     *
     * @return
     */
    private static AllPrices makeSequentialAllPrices() {
        return (tripRequest, currencyConversion) -> {
            List<TripResponse> trips = sFlightPriceProxy
                // Synchronously find all the flights for the
                // tripRequest.
                .findFlightsSync(tripRequest,
                                 Options.instance().maxTimeout());
            Double rate = sExchangeRateProxy
                // Synchronously determine the exchange rate.
                .queryExchangeRateForSync(currencyConversion);

            return convertTripPrices(trips, rate);
        };
    }

    /**
     *
     * @return
     */
    private static AllPrices makeThreadsAllPrices() {
        return (tripRequest, currencyConversion) -> {
            List[] trips = new List[1];
            Double[] rate = new Double[1];

            List<Thread> threads = new ArrayList<Thread>() {
                    {
                        trips[0] = sFlightPriceProxy
                        // Synchronously find all the flights
                        // for the tripRequest.
                        .findFlightsSync(tripRequest,
                                         Options.instance().maxTimeout());
                    }

                    {
                        rate[0] = sExchangeRateProxy
                        // Synchronously determine the exchange rate.
                        .queryExchangeRateForSync(currencyConversion);
                    }
                };

            // Start all the threads.
            threads.forEach(Thread::start);

            // Wait for all the threads to complete.
            threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

            // When the calls to compute the trip and rate complete
            // convert the price.
            return convertTripPrices(trips[0], rate[0]);
        };
    }

    /**
     * 
     * @return
     */
    private static AllPrices makeCompletionServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                CompletionService<Object> cs =
                    new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

                Callable<Object> c1 = () -> sFlightPriceProxy
                    // Synchronously find all the flights for
                    // the tripRequest.
                    .findFlightsSync(tripRequest,
                                     Options.instance().maxTimeout());

                Callable<Object> c2 = () -> sExchangeRateProxy
                    // Synchronously determine the exchange rate.
                    .queryExchangeRateForSync(currencyConversion);

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

                // When the calls to compute the trip and rate complete
                // convert the price.

                return convertTripPrices(trips, rate);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     *
     * @return
     */
    private static AllPrices makeExecutorServiceAllPrices() {
        return (tripRequest, currencyConversion) -> {
            try {
                // Synchronously find all the flights for the tripRequest.
                Callable<List<TripResponse>> c1 = () -> sFlightPriceProxy
                    .findFlightsSync(tripRequest,
                                     Options.instance().maxTimeout());

                // Synchronously determine the exchange rate.
                Callable<Double> c2 = () -> sExchangeRateProxy
                    .queryExchangeRateForSync(currencyConversion);

                // Create a new cached thread pool.
                ExecutorService es = Executors.newCachedThreadPool();

                // Compute the best price and the exchange rate
                // concurrently.
                Future<List<TripResponse>> f1 = es.submit(c1);
                Future<Double> f2 = es.submit(c2);

                // Block until the responses are done.
                List<TripResponse> trips = f1.get();
                Double rate = f2.get();

                // When the calls to compute the trip and rate complete
                // convert the price.
                return convertTripPrices(trips, rate);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    /**
     *
     * @return
     */
    private static BestPrice makeBestPrice() {
        return sBestPriceMap.get(Options.instance().getSync());
    }

    /**
     *
     * @return
     */
    private static AllPrices makeAllPrices() {
        return sAllPricesMap.get(Options.instance().getSync());
    }


    /**
     *
     * @param trips
     * @param rate
     * @return
     */
    private static List<TripResponse> convertTripPrices(List<TripResponse> trips, Double rate) {
        trips.forEach(trip -> trip.convert(rate));
        return trips;
    }
}
