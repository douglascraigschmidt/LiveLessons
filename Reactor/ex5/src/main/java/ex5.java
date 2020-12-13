import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.time.Duration;

/**
 * This program applies WebFlux and Project Reactor features to
 * implement ... between a publisher that runs as a micro-service in
 * one process and produces a flux stream of random integers and a
 * subscriber that runs in one or more threads in a different process
 * and consumes this stream of integers.
 */
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
     * The number of iterations to run the test.
     */
    private static final int sMAX_ITERATIONS = 5;

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
        // Run a test that demonstrates timeouts for Project Reactor
        // concurrent Monos.
        runConcurrentMonos();
    }

    /**
     * Run a test that demonstrates timeouts for Project Reactor
     * concurrent Monos.
     */
    private void runConcurrentMonos() {
        System.out.println("begin runConcurrentMonos()");

        // Create a new proxy that's used to communicate with the
        // FlightProxy microservice.
        FlightPriceProxy flightPriceProxy = new FlightPriceProxy();

        // Create a new proxy that's used to communicate with the
        // Exchangerate microservice.
        ExchangeRateProxy exchangeRateProxy = new ExchangeRateProxy();

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            print("Iteration #" + i);

            Mono<Double> priceM = flightPriceProxy
                // Asynchronously find the best price in US dollars
                // between London and New York.
                .findBestPrice(Schedulers.parallel(),
                               "LDN - NYC");

            Mono<Double> rateM = exchangeRateProxy
                // Asynchronously determine exchange rate between US
                // dollars and British pounds.
                .queryExchangeRateFor(Schedulers.parallel(),
                                      "USD:GBP",
                                      sDEFAULT_RATE_M);

            Mono
                // Call the this::convert method reference to convert
                // the price in dollars to the price in pounds when
                // both previous Monos complete.
                .zip(priceM, rateM, this::convert)

                // If the total async processing takes more than 3
                // seconds a TimeoutException will be thrown.
                .timeout(Duration.ofSeconds(3))

                // Print the price if the call completed within 3
                // seconds.
                .doOnSuccess(amount ->
                             System.out.println("The price is: "
                                                + amount
                                                + " GBP"))
                    
                // Print the TimeoutException if the call took longer
                // than 3 seconds.
                .onErrorResume(ex -> {
                        System.out.println("The exception thrown was "
                                           + ex.toString());
                        return Mono.just(0.0);
                    })

                // Block until all async processing completes.
                .block();
        }

        System.out.println("end runConcurrentMonos()");
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
    
