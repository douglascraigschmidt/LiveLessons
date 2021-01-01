import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously and concurrently reduce, multiply, and display
 * BigFractions via various Mono operations, including fromCallable(),
 * flatMap(), subscribeOn(), zip(), doOnSuccess(), then(), and the
 * parallel thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class MonoEx {
    /**
     * A random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * Test asynchronous BigFraction multiplication using flatMap().
     */
    public static Mono<Void> testFractionMultiplyAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplyAsync()\n");

        return 
            // Make a random BigFraction.
            makeBigFractionAsync(sRandom, sb)

            // Use flatMap() to asynchronously multiply the random
            // BigFraction by a large constant.
            .flatMap(bf -> multiplyAsync(bf, sBigReducedFraction))

            // Display result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zip().
     */
    public static Mono<Void> testFractionCombine()  {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine()\n");

        // Create a random BigFraction asynchronously.
        Mono<BigFraction> m1 = makeBigFractionAsync(sRandom,
                                                    sb);

        // Create another random BigFraction asynchronously.
        Mono<BigFraction> m2 = makeBigFractionAsync(sRandom, 
                                                    sb);

        // Create another random BigFraction asynchronously.
        Mono<BigFraction> m3 = makeBigFractionAsync(sRandom,
                                                    sb);

        // This function is used to combine results from Mono.zip().
        Function<Object[], BigFraction> combiner = bfArray -> Stream
            // Create a stream of Objects.
            .of(bfArray)
            // Convert the Objects to BigFractions.
            .map(o -> BigFraction.valueOf((BigFraction) o))
            // Sum the results together.
            .reduce(BigFraction.valueOf(0), BigFraction::add);

        return Mono
            // Add results after all async multiplications complete.
            .zip(combiner,
                 m1.flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction)),
                 m2.flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
                 m3.flatMap(bf3 -> multiplyAsync(bf3, sBigReducedFraction)))

            // Display result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Asynchronously multiply {@code bf1} and {@code bf2} and return
     * the result as a Mono.
     *
     * @param bf1 The first BigFraction param
     * @param bf2 The first BigFraction param
     * @return A Mono that emits the result of multiplying {@code bf1} and {@code bf2}
     */
    private static Mono<BigFraction> multiplyAsync(BigFraction bf1,
                                                   BigFraction bf2) {
        return Mono
            // Create a Mono that emits the results of multiplying bf1
            // and bf2.
            .fromCallable(() -> bf1.multiply(bf2))

            // Run the processing in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());
    }

    /**
     * A factory method that creates a {@code random} BigFraction
     * asynchronously in a thread pool.
     *
     * @param random The random number generator
     * @param sb The StringBuffer to log the created BigFraction
     * @return A Mono that emits the random BigFraction
     */
    private static Mono<BigFraction> makeBigFractionAsync(Random random,
                                                          StringBuffer sb) {
        return Mono
            // Factory method that makes a random big fraction and
            // multiplies it with a constant.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(random,
                                           true))

            // Run the processing in the parallel thread pool.
            .subscribeOn(Schedulers.parallel())

            // Print result after creating it.
            .doOnSuccess(bf -> appendBigFraction(bf, sb));
    }
}
