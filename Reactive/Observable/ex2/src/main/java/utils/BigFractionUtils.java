package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

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
     * Number of big fractions to process asynchronously in a Reactor
     * flux stream.
     */
    public static final int sMAX_FRACTIONS = 10;

    /**
     * These final strings are used to pass params to various lambdas in the
     * test methods below.
     */
    public static final String sF1 = "62675744/15668936";
    public static final String sF2 = "609136/913704";
    public static final String sBI1 = "846122553600669882";
    public static final String sBI2 = "188027234133482196";

    /**
     * A big reduced fraction constant.
     */
    public static final BigFraction sBigReducedFraction =
        BigFraction.valueOf(new BigInteger("846122553600669882"),
                            new BigInteger("188027234133482196"),
                            true);

    /**
     * Stores a completed mono with a value of sBigReducedFraction.
     */
    public static final Single<BigFraction> mBigReducedFractionS =
        Single.just(sBigReducedFraction);

    /**
     * Represents a test that's completed running when it returns.
     */
    public static final Completable sVoid =
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
}
