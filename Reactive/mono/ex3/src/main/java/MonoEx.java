import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously and concurrently reduce, multiply, and display
 * BigFractions via various Mono operations, including fromCallable(),
 * flatMap(), subscribeOn(), zipWith(), zip(), doOnSuccess(), then(),
 * and the parallel thread pool.
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

        // Make a random BigFraction.
        Mono<BigFraction> bfM1 = makeBigFractionAsync(sRandom, sb);

        // Use flatMap() to asynchronously multiply the random
        // BigFraction by a large constant and avoid "nested" monos.
        Mono<BigFraction> bfM2 = bfM1
            .flatMap(bf -> multiplyAsync(bf, sBigReducedFraction));

        // This is what the code would look like if flatMap() was
        // replaced by map()!
        // Mono<Mono<BigFraction>> bfM2 = bfM1
        //         .map(bf -> multiplyAsync(bf, sBigReducedFraction));

        return bfM2
            // Display result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Mono<Void> testFractionCombine1()  {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine1()\n");

        // Create a random BigFraction asynchronously.
        Mono<BigFraction> m1 = makeBigFractionAsync(sRandom, sb);

        // Create another random BigFraction asynchronously.
        Mono<BigFraction> m2 = makeBigFractionAsync(sRandom, sb);

        return m1
            // Multiply two BigFractions after the random BigFraction
            // completes its initialization.
            .flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction))
            
            // Add results after both async multiplications complete.
            .zipWith(m2
                     // Multiply two BigFractions after the random
                     // BigFraction completes its initialization.
                     .flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
                     BigFraction::add)

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
    public static Mono<Void> testFractionCombine2()  {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine2()\n");

        // Create a random BigFraction asynchronously.
        Mono<BigFraction> m1 = makeBigFractionAsync(sRandom, sb);

        // Create another random BigFraction asynchronously.
        Mono<BigFraction> m2 = makeBigFractionAsync(sRandom, sb);

        // Create another random BigFraction asynchronously.
        Mono<BigFraction> m3 = makeBigFractionAsync(sRandom, sb);

        // This function combines results from Mono.zip().
        Function<Object[], BigFraction> combinator = bfArray -> Stream
            // Create a stream of Objects.
            .of(bfArray)

            // Convert the Objects to BigFractions.
            .map(o -> BigFraction.valueOf((BigFraction) o))

            // Sum the results together.
            .reduce(BigFraction.valueOf(0), BigFraction::add);

        // This array holds results of multiple async multiplications.
        @SuppressWarnings("unchecked")
        Mono<BigFraction>[] asyncMultiplications = new Mono[] {
            // Multiply BigFractions after the random BigFractions
            // completes its initialization.
            m1.flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction)),
            m2.flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
            m3.flatMap(bf3 -> multiplyAsync(bf3, sBigReducedFraction))
        };

        return Mono
            // The combiner adds results after all
            // asyncMultiplications complete.
            .zip(combinator, asyncMultiplications)

            // Display reduced result after converting it to a mixed
            // fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return an empty Mono to synchronize with the
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
            // Factory method that makes a random big fraction.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(random,
                                           true))

            // Run the processing in the parallel thread pool.
            .subscribeOn(Schedulers.parallel())

            // Print result after creating it.
            .doOnSuccess(bf -> BigFractionUtils
                         .appendBigFraction(bf, sb));
    }
}
