package tests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tests.Utils;

import java.time.Duration;

import static tests.Utils.*;

/**
 * This class demonstrates timeouts for the Project Reactor
 * {@link Mono} reactive type.
 */
public class ConcurrentMonos {
    /**
     * Run a test that demonstrates timeouts for Project Reactor
     * concurrent {@link Mono} objects.
     */
    public static void runTest(int iterations) {
        System.out.println("begin runConcurrentMonos()");

        Flux
            // Run the test multiple times.
            .range(1, iterations)

            // Apply the flatMap concurrency idiom to compute the
            // results concurrently.
            .flatMap(ConcurrentMonos::computePriceConcurrent)

            // Wait until all the results are complete.
            .blockLast();

        System.out.println("end runConcurrentMonos()");
    }

    /**
     * Compute all the flight prices concurrently.
     *
     * @param iteration The iteration of the test
     * @return A {@link Mono<Void>} that indicate the async
     *         computation is done
     */
    private static Mono<Void> computePriceConcurrent(long iteration) {
        StringBuffer sb = new StringBuffer("Iteration #" + iteration + ": ");

        Mono<Double> priceS = Mono
            // Asynchronously find the best price in US dollars
            // between London and New York.
            .fromCallable(() -> Utils
                          .findBestPrice("LDN - NYC", sb))

            // Run computation in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        Mono<Double> rateS = Mono
            // Asynchronously determine exchange rate between US
            // dollars and British pounds.
            .fromCallable(() -> Utils
                          .queryExchangeRateFor("USD:GBP",
                                                sb))

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), sDEFAULT_RATE_S)

            // Run computation in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        return Mono
            // The this::convert method reference converts the price
            // in dollars to the price in pounds when both previous
            // Mono objects complete.
            .zip(priceS, rateS, Utils::convert)

            // If async processing takes more than 3 seconds a
            // TimeoutException will be thrown.
            .timeout(Duration.ofSeconds(3))

            // Log the occurrence of the TimeoutException and resume
            // processing.
            .onErrorResume(ex -> {
                    sb.append("\nThe exception thrown was "
                              + ex.toString());
                    return Mono.just(0.0);
                })

            // Print the results depending on the outcome.
            .doOnSuccess(amount -> {
                    if (amount != 0.0)
                        sb.append("\nThe price is " + amount + " GBP");
                    else
                        sb.append("\nThe computation timed out");

                    // Display the results.
                    Utils.display(sb.toString());
                })

            // Indicate the async computation is complete.
            .then();
    }
}

