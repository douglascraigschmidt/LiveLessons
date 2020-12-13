import proxies.ExchangeRateProxy;
import proxies.FlightPriceProxy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTester;
import utils.Options;
import utils.RunTimer;

import java.time.Duration;
import java.util.function.Function;

/**
 * This program applies WebFlux and Project Reactor features to
 * implement an airline reservations app that synchronously and
 * asynchronously communicates with various microservices that find
 * the best price for flight legs and convert from US dollars into
 * other currencies.  The best price is displayed after the
 * microservices have completed their computations.
 */
@SuppressWarnings("ConstantConditions")
public class ex5 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The default exchange rate if a timeout occurs.
     */
    private static final Mono<Double> sDEFAULT_RATE_M =
        Mono.just(1.0);

    /**
     * The maximum amount of time to wait for all the asynchronous
     * processing to complete.
     */
    private static final Duration sMAX_TIME =
        Duration.ofSeconds(3);

    /**
     * The number of iterations to run the test.
     */
    private static final int sMAX_ITERATIONS = 5;

    /**
     * A proxy that's used to communicate with the FlightPrice
     * microservice.
     */
    FlightPriceProxy mFlightPriceProxy = new FlightPriceProxy();

    /**
     * A proxy that's used to communicate with the ExchangeRate
     * microservice.
     */
    ExchangeRateProxy mExchangeRateProxy = new ExchangeRateProxy();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(args);

        // Run the test program.
        new ex5().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        // This test invokes microservices to asynchronously determine
        // the best price for a flight from London to New York city in
        // British pounds.
        RunTimer.timeRun(this::runAsyncMonos, "runAsyncMonos");

        // This test invokes microservices to synchronously determine
        // the best price for a flight from London to New York city in
        // British pounds.
        RunTimer.timeRun(this::runSyncMonos, "runSyncMonos");

        // Print the results sorted from fastest to slowest.
        print(RunTimer.getTimingResults());
    }

    /**
     * This test invokes microservices to asynchronously determine the
     * best price for a flight from London to NYC in British pounds.
     */
    private void runAsyncMonos() {
        System.out.println("begin runAsyncMonos()");

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            int iteration = i + 1;

            // Register the test with the AsyncTester framework so it
            // will run asynchronously wrt the other iterations.
            AsyncTester.register(() -> getBestPriceInPoundsAsync(iteration));
        }

        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .block();

        System.out.println("end runAsyncMonos()");
    }

    /**
     * This test invokes microservices to synchronously determine the
     * best price for a flight from London to NYC in British pounds.
     */
    private void runSyncMonos() {
        System.out.println("begin runSyncMonos()");

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++)
            // Call the test synchronously.
            getBestPriceInPoundsSync(i + 1);

        System.out.println("end runSyncMonos()");
    }

    /**
     * Returns the best price for a flight from London to NYC in
     * British pounds via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @return An empty Mono to synchronize with the AsyncTester framework.
     */
    private Mono<Void> getBestPriceInPoundsAsync(int iteration) {
        Mono<Double> priceM = mFlightPriceProxy
            // Asynchronously find the best price in US dollars
            // between London and New York city.
            .findBestPriceAsync(Schedulers.parallel(),
                                "LDN - NYC");

        Mono<Double> rateM = mExchangeRateProxy
            // Asynchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForAsync(Schedulers.parallel(),
                                       "USD:GBP",
                                       sDEFAULT_RATE_M);

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
            ? extends Mono<? extends Double>> handleEx = ex -> {
            print("Iteration #"
                  + iteration
                  + " The exception thrown was " + ex.toString());
            return Mono.just(0.0);
        };

        // When priceM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these async
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        return combineAndConvertResults(priceM, rateM, sMAX_TIME)
            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnSuccess(amount ->
                         print("Iteration #"
                               + iteration
                               + " The price is: "
                               + amount
                               + " GBP"))
                    
            // Consume and print the TimeoutException if the call
            // took longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Return an empty mono to synchronize with the
            // AsyncTester framework.
            .then();
    }

    /**
     * Returns the best price for a flight from London to NYC in
     * British pounds via synchronous computations.
     *
     * @param iteration Current iteration count
     */
    private void getBestPriceInPoundsSync(int iteration) {
        Mono<Double> priceM = mFlightPriceProxy
            // Synchronously find the best price in US dollars between
            // London and New York city.
            .findBestPriceSync("LDN - NYC");

        Mono<Double> rateM = mExchangeRateProxy
            // Synchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForSync("USD:GBP",
                                      sDEFAULT_RATE_M);

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
                ? extends Mono<? extends Double>> handleEx = ex -> {
            print("Iteration #"
                  + iteration
                  + " The exception thrown was " + ex.toString());
            return Mono.just(0.0);
        };

        // When priceM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these sync
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        combineAndConvertResults(priceM, rateM, sMAX_TIME)
            // Print the price if the call completed within sMAX_TIME
            // seconds.
            .doOnSuccess(amount ->
                         print("Iteration #"
                               + iteration
                               + " The price is: "
                               + amount
                               + " GBP"))
                    
            // Consume and print the TimeoutException if the call took
            // longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Block until the computation is done.
            .block();
    }

    /**
     * When {@code priceM} and {@code rateM} complete convert the
     * price in US dollars to the price in British pounds.  If these
     * operations take more than {@code maxTime} then throw the
     * TimeoutException.
     *
     * @param priceM Returns the best price for a flight leg
     * @param rateM Returns the exchange rate
     * @param maxTime Max time to wait for processing to complete
     @ return A conversion of best price into British pounds
     */
    private Mono<Double> combineAndConvertResults(Mono<Double> priceM,
                                                  Mono<Double> rateM,
                                                  Duration maxTime) {
        return Mono
            // Call the this::convert method reference to convert the
            // price in dollars to the price in pounds when both
            // previous Monos complete their processing.
            .zip(priceM, rateM, this::convert)

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }

    /**
     * Convert a price in one currency system by multiplying it by the
     * exchange rate.
     */
    private double convert(double price, double rate) {
        return price * rate;
    }

    /**
     * Print the {@code string} together with thread information.
     */
    private void print(String string) {
        System.out.println("Thread["
                           + Thread.currentThread().getId()
                           + "]: "
                           + string);
    }
}
    
