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
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html
 */
public class FluxEx {
    /**
     * Test BigFraction multiplication using a synchronous Flux
     * stream.
     */
    public static Mono<Void> testFractionMultiplication() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplication()\n");

        Flux
            // Use just() to generate a stream of big fractions.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#just-T...-
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Use map() to multiply each element in the stream by a
            // constant.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-
            .map(fraction -> {
                    sb.append("     "
                              + fraction.toMixedString()
                              + " x "
                              + sBigReducedFraction.toMixedString());

                    // Multiply and return result.
                    return fraction.multiply(sBigReducedFraction);
                })

            // Use subscribe() to initiate all the processing and
            // handle the results.  This call runs synchronously since
            // the publisher (just()) is synchronous and runs in the
            // calling thread.  However, there are more interesting
            // types of publishers that enable asynchrony.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#subscribe-java.util.function.Consumer-java.util.function.Consumer-java.lang.Runnable-
            .subscribe(// Handle next event.
                       fraction ->
                       // Add fraction to the string buffer.
                       sb.append(" = " + fraction.toMixedString() + "\n"),

                       // Handle error result event.
                       error -> sb.append("error"),

                       // Handle final completion event.
                       () ->
                       // Display results when processing is done.
                       BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTester that all
        // the processing is done.
        return sVoidM;
    }
}
