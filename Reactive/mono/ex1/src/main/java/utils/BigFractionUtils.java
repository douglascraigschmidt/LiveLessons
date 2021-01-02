package utils;

/**
 * A utility class containing helpful methods for manipulating various
 * BigFraction features.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
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
     * Display the {@code string} after prepending the thread id.
     */
    public static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }

    /**
     * Convert {@code bigFraction} to a mixed string and display it
     * and the contents of {@code stringBuilder}.
     */
    public static void displayMixedBigFraction(String bigFraction,
                                               StringBuilder stringBuilder) {
        stringBuilder.append("     ["
                             + Thread.currentThread().getId()
                             + "] Mixed BigFraction result = "
                             + bigFraction
                             + "\n");
        BigFractionUtils.display(stringBuilder.toString());
    }

    /**
     * Convert {@code bigFraction} to a mixed string and display it
     * and the contents of {@code stringBuilder}.
     */
    public static void logBigFraction(BigFraction unreducedFraction,
                                      BigFraction reducedFraction,
                                      StringBuilder stringBuilder) {
        stringBuilder
            .append("     unreducedFraction "
                    + unreducedFraction.toString()
                    + "\n     reduced improper fraction = "
                    + reducedFraction.toString()
                    + "\n     calling BigFraction::toMixedString\n");
    }
}
