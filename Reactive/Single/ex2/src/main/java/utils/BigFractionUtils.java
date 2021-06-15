package utils;

import io.reactivex.rxjava3.core.Completable;

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
    public static final Completable sVoidS =
        Completable.complete();

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

    /**
     * Convert {@code bigFraction} to a mixed string and display it
     * and the contents of {@code stringBuffer}.
     */
    public static void displayMixedBigFraction(String bigFraction,
                                               StringBuffer stringBuffer) {
        stringBuffer.append("     ["
                + Thread.currentThread().getId()
                + "] Mixed BigFraction result = "
                + bigFraction
                + "\n");
        BigFractionUtils.display(stringBuffer.toString());
    }

    /**
     * Convert {@code bigFraction} to a mixed string and display it
     * and the contents of {@code stringBuffer}.
     */
    public static void logBigFraction(BigFraction unreducedFraction,
                                      BigFraction reducedFraction,
                                      StringBuffer sb) {
        sb.append("     ["
                  + Thread.currentThread().getId()
                  + "] unreducedFraction "
                  + unreducedFraction.toString()
                  + "\n     reduced improper fraction = "
                  + reducedFraction.toString()
                  + "\n     calling BigFraction::toMixedString\n");
    }
}
