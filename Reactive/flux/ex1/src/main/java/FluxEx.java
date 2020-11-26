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

        Flux
            // Use just() to generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Use map() to multiply each element in the stream by a
            // constant.
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
            .subscribe(// Handle next event.
                       multipliedFraction ->
                       // Add fraction to the string buffer.
                       sb.append(" = " + multipliedFraction.toMixedString() + "\n"),

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
