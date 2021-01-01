package utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.util.*;

/**
 * A utility class containing helpful methods for manipulating various
 * BigFraction features.
 */
public class BigFractionUtils {
    /**
     * A utility class should always define a private constructor.
     */
    private BigFractionUtils() {
    }

    /**
     * A big reduced fraction constant.
     */
    public static final BigFraction sBigReducedFraction =
            BigFraction.valueOf(new BigInteger("846122553600669882"),
                    new BigInteger("188027234133482196"),
                    true);

    /**
     * A factory method that returns a large random BigFraction whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A large random BigFraction
     */
    public static BigFraction makeBigFraction(Random random,
                                              boolean reduced) {
        // Create a large random big integer.
        BigInteger numerator =
            new BigInteger(150000, random);

        // Create a denominator that's between 1 to 10 times smaller
        // than the numerator.
        BigInteger denominator =
            numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

        // Return a big fraction.
        return BigFraction.valueOf(numerator,
                                   denominator,
                                   reduced);
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    public static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }

    /**
     * Append {@code bigFraction} to the {@code stringBuffer}.
     */
    public static void appendBigFraction(BigFraction bigFraction,
                                         StringBuffer stringBuffer) {
        stringBuffer.append("     ["
                            + Thread.currentThread().getId()
                            + "] BigFraction = "
                            + bigFraction.toString()
                            + "\n");
    }

    /**
     * Convert {@code bigFraction} to a mixed string and display it and
     * the contents of {@code stringBuffer}.
     */
    public static void displayMixedBigFraction(BigFraction bigFraction,
                                               StringBuffer stringBuffer) {
        stringBuffer.append("     ["
                            + Thread.currentThread().getId()
                            + "] Mixed BigFraction result = "
                            + bigFraction.toMixedString()
                            + "\n");
        BigFractionUtils.display(stringBuffer.toString());
    }
}
