import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Arrays;
import java.util.List;
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
        Single<BigFraction> bfS1 = makeBigFractionAsync(sRandom, sb);

        // Use flatMap() to asynchronously multiply the random
        // BigFraction by a large constant and avoid "nested" monos.
        Single<BigFraction> bfS2 = bfS1
            .flatMap(bf -> multiplyAsync(bf, sBigReducedFraction));

        // This is what the code would look like if flatMap() was
        // replaced by map()!
        // Single<Single<BigFraction>> bfS2 = bfM1
        //         .map(bf -> multiplyAsync(bf, sBigReducedFraction));

        return bfS2
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
        Single<BigFraction> s1 = makeBigFractionAsync(random, sb);

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Single<BigFraction> s2 = makeBigFractionAsync(random, sb);
        
        return s1
            // Multiply two BigFractions after the random BigFraction
            // completes its initialization.
            .flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction))

            // Add results after both async multiplications complete.
            .zipWith(s2
                     // Multiply two BigFractions after the random
                     // BigFraction completes its initialization.
                     .flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

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
        final Single<BigFraction> m1 = makeBigFractionAsync(sRandom, sb);

        // Create another random BigFraction asynchronously.
        final Single<BigFraction> m2 = makeBigFractionAsync(sRandom, sb);

        // Create another random BigFraction asynchronously.
        final Single<BigFraction> m3 = makeBigFractionAsync(sRandom, sb);

        // This function combines results from Single.zipArray().
        Function<Object[], BigFraction> zipper = bfArray -> Stream
            // Create a stream of Objects.
            .of(bfArray)

            // Convert the Objects to BigFractions.
            .map(o -> BigFraction.valueOf((BigFraction) o))
                
            // Sum the results together.
            .reduce(BigFraction.valueOf(0), BigFraction::add);

        // This array holds results of multiple async multiplications.
        @SuppressWarnings("unchecked")
        SingleSource<BigFraction>[] asyncMultiplications = new SingleSource[] {
            // Multiply BigFractions after the random BigFractions
            // completes its initialization.
            m1.flatMap(bf1 -> multiplyAsync(bf1, sBigReducedFraction)),
            m2.flatMap(bf2 -> multiplyAsync(bf2, sBigReducedFraction)),
            m3.flatMap(bf3 -> multiplyAsync(bf3, sBigReducedFraction))
        };

        return Single
            // The zipper adds results after all asyncMultiplications
            // complete.
            .zipArray(zipper, asyncMultiplications)

            // Display reduced result after converting it to a mixed
            // fraction.
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
