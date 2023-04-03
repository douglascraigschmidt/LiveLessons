package tests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static tests.Utils.sDEFAULT_RATE_F;

/**
 * This class demonstrates timeouts for the Project Reactor
 * {@link ParallelFlux} reactive type.
 */
public class ParallelFluxes {
    /**
     * Run a test that demonstrates timeouts for Project Reactor
     * {@link ParallelFlux} objects.
     */
    public static void runTest(int iterations) {
        System.out.println("begin runParallelFluxs()");

        // Iterate multiple times.
        Flux
            // Run the test multiple times.
            .range(1, iterations)

            // Apply the flatMap concurrency idiom to compute the
            // results concurrently.
            .flatMap(ParallelFluxes::computePriceParallel)

            // Wait until all the results are complete.
            .blockLast();

        System.out.println("end runParallelFluxs()");
    }
    
    /**
     * Compute all the flight prices in parallel.
     *
     * @param iteration The iteration of the test
     * @return A {@link Mono<Void>} that indicate the async
     *         computation is done
     */
    private static Mono<Void> computePriceParallel(long iteration) {
        StringBuffer sb = new StringBuffer("Iteration #" + iteration + ": ");

        ParallelFlux<Double> priceF = Flux
            // Asynchronously find the best price in US dollars from
            // London to New York.
            .just("LDN:NYC")

            // Run computation in the parallel thread pool.
            .parallel().runOn(Schedulers.parallel())

            // Find the best price.
            .map(sourceDest -> Utils.findBestPrice(sourceDest, sb));

        Flux<Double> rateF = Flux
            // Asynchronously determine exchange rate from British
            // pounds to US dollars.
            .just("GBP:USA")

            // Run computation in the parallel thread pool.
            .parallel().runOn(Schedulers.parallel())

            // Find the exchange rate.
            .map(sourceDest -> Utils
                 .queryExchangeRateFor(sourceDest, sb))

            // Convert back to sequential since timeout() isn't
            // defined on a ParallelFlux.
            .sequential()

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), sDEFAULT_RATE_F);

        return Flux
            // The this::convert method reference converts the price
            // in dollars to the price in pounds when both previous
            // Flux objects complete.
            .zip(priceF, rateF, Utils::convert)

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
            .doOnNext(amount -> {
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

