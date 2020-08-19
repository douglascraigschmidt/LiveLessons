import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously and concurrently reduce, multiply, and display
 * BigFractions via various Mono operations, including fromCallable(),
 * subscribeOn(), zipWith(), doOnSuccess(), then(), and the parallel
 * thread pool.
 */
public class MonoEx {
    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Mono<Void> testFractionCombine() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m1 = makeBigFraction(random, sb);

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m2 = makeBigFraction(random, sb);
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combining result = "
                      + bigFraction.toMixedString()
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return m1
            // Add BigFraction results after m1 and m2 both complete.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#zipWith-reactor.core.publisher.Mono-java.util.function.BiFunction-
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#doOnSuccess-java.util.function.Consumer-
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty mono to synchronize with the
            // AsyncTester framework.
             // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#then--
           .then();
    }

    /**
     * A factory method that creates a random big fraction and
     * subscribes it to be reduced and multiplied in a thread pool.
     */
    private static Mono<BigFraction> makeBigFraction(Random random,
                                                     StringBuffer sb) {
        // Create a consumer that prints the result as a mixed
        // fraction after it's multiplied.
        Consumer<BigFraction> fractionPrinter = bigFraction
                -> {
            sb.append("     ["
                      + Thread.currentThread().getId()
                      + "] bigFraction = "
                    + bigFraction.toMixedString()
                    + "\n");
        };

        return Mono
            // Factory method that makes a random big fraction and
            // multiplies it with a constant.
            .just(BigFractionUtils
                  .makeBigFraction(random, 
                                   true)
                  .multiply(sBigReducedFraction))

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.parallel())

            // Print result after multiplying it.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#doOnSuccess-java.util.function.Consumer-
            .doOnSuccess(fractionPrinter);
    }
}
