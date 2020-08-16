package utils;

import reactor.core.publisher.Mono;

import java.math.BigInteger;

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
     * These final strings are used to pass params to various lambdas in the
     * test methods below.
     */
    public static final String sBI1 = "846122553600669882";
    public static final String sBI2 = "188027234133482196";

    /**
     * A big reduced fraction constant.
     */
    public static final BigFraction sBigReducedFraction =
            BigFraction.valueOf(new BigInteger(sBI1),
                    new BigInteger(sBI2),
                    true);

    /**
     * Represents a test that's completed running when it returns.
     */
    public static final Mono<Void> sVoidM =
            Mono.empty();

    /**
     * Display the {@code string} after prepending the thread id.
     */
    public static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }
}
