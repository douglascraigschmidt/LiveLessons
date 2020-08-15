import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;

import static utils.BigFractionUtils.sBigReducedFraction;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously and concurrently reduce, multiply, and display
 * BigFractions via various Mono operations, including fromCallable(),
 * subscribeOn(), zipWith(), doOnSuccess(), ambArray(),
 * ignoreElement(), and the parallel thread pool.
 */
public class SingleEx {
    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Completable testFractionCombine() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Single<BigFraction> m1 = makeBigFraction(random, sb);

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Single<BigFraction> m2 = makeBigFraction(random, sb);
        
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
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty single to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * A factory method that creates a random big fraction and
     * subscribes it to be reduced and multiplied in a thread pool.
     */
    private static Single<BigFraction> makeBigFraction(Random random,
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

        return Single
            // Factory method that makes a random big fraction and
            // multiplies it with a constant.
            .just(BigFractionUtils
                  .makeBigFraction(random, 
                                   true)
                  .multiply(sBigReducedFraction))

            // Print result after multiplying it.
            .doOnSuccess(fractionPrinter)

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.computation());
    }
}
