package ex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.concurrent.CountDownLatch;

import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.BigFractionUtils.sVoidM;

/**
 * This class shows how to apply Project Reactor features
 * synchronously to perform basic Flux operations, including just(),
 * map(), and subscribe().
 */
public class FluxEx {
    /**
     * Test BigFraction multiplication using a synchronous Flux
     * stream.
     */
    public static Mono<Void> testFractionMultiplication() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplication()\n");

        // Create a synchronizer to manage completion.
        CountDownLatch latch = new CountDownLatch(1);

        Flux
            // Use a Flux to generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Perform a multiplication on each element in the stream.
            .map(fraction -> {
                    sb.append("     "
                              + fraction.toMixedString()
                              + " x "
                              + sBigReducedFraction.toMixedString());

                    // Multiply and return result.
                    return fraction.multiply(sBigReducedFraction);
                })

            // Start all the processing in motion.
            .subscribe(fraction ->
                       // Add next fraction to the string buffer.
                       sb.append(" = " + fraction.toMixedString() + "\n"),

                       // Handle error result.
                       error -> sb.append("error"),

                       // Handle final completion.
                       () -> {
                           // Display results when all processing is done.
                           BigFractionUtils.display(sb.toString());

                           // Release the latch.
                           latch.countDown();
                       });

        try {
            // Wait for the flux to complete all its processing.
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return empty mono.
        return sVoidM;
    }
}
