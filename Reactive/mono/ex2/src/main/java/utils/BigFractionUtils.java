package utils;

import reactor.core.publisher.Mono;

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
    public static final String sF1 = "62675744/15668936";
    public static final String sF2 = "609136/913704";
    public static final String sBI1 = "846122553600669882";
    public static final String sBI2 = "188027234133482196";

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
