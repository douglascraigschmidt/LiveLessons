import proxies.ExchangeRateProxy;
import proxies.FlightPriceProxy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTester;
import utils.Options;

import java.time.Duration;
import java.util.function.Function;

/**
 * This program applies WebFlux and Project Reactor features to
 * implement a flight booking app that asynchronously communicates
 * with a FlightPrice microservice in one process and an ExchangeRate
 * microservice in another process and then displays the results in
 * this program after both microservices have completed their
 * asynchronous computations.
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

    // Create a new proxy that's used to communicate with the
    // FlightProxy microservice.
    FlightPriceProxy mFlightPriceProxy = new FlightPriceProxy();

    // Create a new proxy that's used to communicate with the
    // Exchangerate microservice.
    ExchangeRateProxy mExchangeRateProxy = new ExchangeRateProxy();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) throws InterruptedException {
        // Parse the command-line arguments.
        Options.instance().parseArgs(args);

        // Run the test program.
        new ex5().run();
    }

    /**
     * Run the test program.
     */
    private void run() throws InterruptedException {
        // Run a test that demonstrates timeouts for Project Reactor
        // concurrent Monos.
        runAsyncMonos();
    }

    /**
     * Run a test that invokes calls on microservices to asynchronously
     * determine the best price for a flight from London to New York city
     * and the current exchange rate of US dollars to British pounds.
     */
    private void runAsyncMonos() throws InterruptedException {
        System.out.println("begin runAsyncMonos()");

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            int iteration = i + 1;
            AsyncTester.register(() -> getBestPriceInPounds(iteration));
        }

        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .block();

        System.out.println("end runAsyncMonos()");
    }

    private Mono<Void> getBestPriceInPounds(int iteration) {
        Mono<Double> priceM = mFlightPriceProxy
            // Asynchronously find the best price in US dollars
            // between London and New York.
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
        // operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertAsyncResults(priceM, rateM, sMAX_TIME)
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
     * When {@code priceM} and {@code rateM} complete convert the
     * price in US dollars to the price in British pounds.  If these
     * async operations take more than {@code maxTime} then throw the
     * TimeoutException.
     *
     * @param priceM Returns the best price for a flight leg
     * @param rateM Returns the exchange rate
     * @param maxTime Max time to wait for async processing to complete
     @ return A conversion of best price into British pounds
     */
    private Mono<Double> combineAndConvertAsyncResults(Mono<Double> priceM,
                                                       Mono<Double> rateM,
                                                       Duration maxTime) {
        return Mono
            // Call the this::convert method reference to convert the
            // price in dollars to the price in pounds when both
            // previous Monos complete their async processing.
            .zip(priceM, rateM, this::convert)

            // If the total async processing takes more than maxTime a
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
    
