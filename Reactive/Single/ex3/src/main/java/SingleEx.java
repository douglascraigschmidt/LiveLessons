import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.stream.Stream;

import static utils.BigFractionUtils.displayMixedBigFraction;
import static utils.BigFractionUtils.sBigReducedFraction;

/**
 * This class shows how to apply RxJava features asynchronously and
 * concurrently reduce, multiply, and display BigFractions via various
 * Single operations, including fromCallable(), subscribeOn(),
 * zipArray(), zipWith(), doOnSuccess(), ignoreElement(), and
 * Schedulers.computation().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class SingleEx {
    /**
     * A random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * Test asynchronous BigFraction multiplication using flatMap().
     */
    public static Completable testFractionMultiplyAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplyAsync()\n");

        // Make a random BigFraction.
        Single<BigFraction> bfM1 = makeBigFractionAsync(sRandom, sb);

        // Use flatMap() to asynchronously multiply the random
        // BigFraction by a large constant and avoid "nested" monos.
        Single<BigFraction> bfM2 = bfM1
            .flatMap(bf -> multiplyAsync(bf, sBigReducedFraction));

        // This is what the code would look like if flatMap() was
        // replaced by map()!
        // Single<Single<BigFraction>> bfM3 = bfM1
        //         .map(bf -> multiplyAsync(bf, sBigReducedFraction));

        return bfM2
            // Display result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return a Completable to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Completable testFractionCombine1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine1()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Single<BigFraction> m1 = makeBigFractionAsync(random, sb);

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Single<BigFraction> m2 = makeBigFractionAsync(random, sb);
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction -> {
            sb.append("     combining result = "
                      + bigFraction.toMixedString()
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return m1
            // Add BigFraction results after m1 and m2 both complete.
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return a Completable to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zip().
     */
    public static Completable testFractionCombine2()  {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine2()\n");

        // Create a random BigFraction asynchronously.
        Single<BigFraction> m1 = makeBigFractionAsync(sRandom,
                                                      sb);

        // Create another random BigFraction asynchronously.
        Single<BigFraction> m2 = makeBigFractionAsync(sRandom, 
                                                      sb);

        // Create another random BigFraction asynchronously.
        Single<BigFraction> m3 = makeBigFractionAsync(sRandom,
                                                      sb);

        // This function is used to combine results from Mono.zip().
        Function<Object[], BigFraction> combiner = bfArray -> Stream
            // Create a stream of Objects.
            .of(bfArray)
            // Convert the Objects to BigFractions.
            .map(o -> BigFraction.valueOf((BigFraction) o))
            // Sum the results together.
            .reduce(BigFraction.valueOf(0), BigFraction::add);

        return Single
            // Add results after all async multiplications complete.
            .zipArray(combiner,
                      m1.flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction)),
                      m2.flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
                      m3.flatMap(bf3 -> multiplyAsync(bf3, sBigReducedFraction)))

            // Display result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return a Completable to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * Asynchronously multiply {@code bf1} and {@code bf2} and return
     * the result as a Single.
     *
     * @param bf1 The first BigFraction param
     * @param bf2 The first BigFraction param
     * @return A Single that emits the result of multiplying {@code bf1} and {@code bf2}
     */
    private static Single<BigFraction> multiplyAsync(BigFraction bf1,
                                                     BigFraction bf2) {
        return Single
            // Create a Single that emits the results of multiplying
            // bf1 and bf2.
            .fromCallable(() -> bf1.multiply(bf2))

            // Run the processing in the parallel thread pool.
            .subscribeOn(Schedulers.computation());
    }

    /**
     * A factory method that creates a {@code random} BigFraction
     * asynchronously in a thread pool.
     *
     * @param random The random number generator
     * @param sb The StringBuffer to log the created BigFraction
     * @return A Single that emits the random BigFraction
     */
    private static Single<BigFraction> makeBigFractionAsync(Random random,
                                                            StringBuffer sb) {
        // Create a consumer that prints the result as a mixed
        // fraction after it's multiplied.
        Consumer<BigFraction> fractionPrinter = bigFraction -> sb
            .append("     ["
                    + Thread.currentThread().getId()
                    + "] bigFraction = "
                    + bigFraction.toMixedString()
                    + "\n");

        return Single
            // Factory method that makes a random big fraction and
            // multiplies it with a constant.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(random, 
                                           true)
                          .multiply(sBigReducedFraction))

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.computation())

            // Print result after multiplying it.
            .doOnSuccess(fractionPrinter);
    }
}
