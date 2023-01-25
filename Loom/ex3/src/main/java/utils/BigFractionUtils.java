package utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static jdk.incubator.concurrent.StructuredTaskScope.*;

/**
 * This Java utility class contains static methods and fields useful
 * for processing {@link BigFraction} objects.
 */
public final class BigFractionUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private BigFractionUtils() {}

    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    public static final int sMAX_FRACTIONS = 10;

    /**
     * These final strings are used to pass params to various lambdas
     * in the test methods below.
     */
    public static final String sF1 = "62675744/15668936";
    public static final String sF2 = "609136/913704";
    public static final String sBI1 = "846122553600669882";
    public static final String sBI2 = "188027234133482196";

    /**
     * Create a new unreduced {@link BigFraction}.
     */
    public static final BigFraction sUnreducedFraction =
        BigFraction.valueOf(new BigInteger(sBI1),
                            new BigInteger(sBI2),
                            false);

    /**
     * Represents a test that's already completed running when it
     * returns.
     */
    public static final CompletableFuture<Void> sCompleted =
        CompletableFuture.completedFuture(null);

    /**
     * A reduced {@link BigFraction} fraction constant.
     */
    public static final BigFraction sBigReducedFraction =
        BigFraction.valueOf(new BigInteger(sBI1),
                            new BigInteger(sBI2),
                            true);

    /**
     * A factory method that returns a large random {@link
     * BigFraction} whose creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction
     *                or not
     * @return A large random {@link BigFraction}
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
     * Sort the {@code List} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuilder}
     * parameter.
     */
    public static void sortAndPrintList(List<Future<BigFraction>> list) {
        try (var scope = new ShutdownOnSuccess<List<BigFraction>>()) {
            // This implementation uses quick sort to order the list.
            var quickSortF = scope
                // Perform quick sort asynchronously.
                .fork(() -> quickSort(FutureUtils
                                          // Convert List<Future> to List.
                                          .futures2Objects(list)));

            // This implementation uses heap sort to order the list.
            var heapSortF = scope
                // Perform heap sort asynchronously.
                .fork(() -> heapSort(FutureUtils
                                         // Convert List<Future> to List.
                                         .futures2Objects(list)));

            // This barrier synchronizer waits for all threads to
            // finish or the task scope to shut down.
            scope.join();

            // Select the result of whichever sort implementation
            // finishes first and use it to print the sorted list.
            scope
                .result()
                .forEach(fraction -> System.out
                         .println(fraction.toMixedString()));
        } catch (Exception exception) {
            System.out.println("Exception: " 
                               + exception.getMessage());
        }
    }

    /**
     * Perform a quick sort on the {@code list}.
     */
    private static <T> List<T> quickSort
        (List<T> list) {
        List<T> copy = new ArrayList<>(list);
    
        // Order the list with quick sort.
        copy.sort(null);

        return copy;
    }

    /*
     * Perform a heap sort on the {@code list}.
     */
    private static <T extends Comparable<? super T>> List<T> heapSort
        (List<T> list) {
        List<T> copy = new ArrayList<>(list);

        // Order the list with heap sort.
        HeapSort.sort(copy);

        return copy;
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    public static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().threadId()
                           + "] "
                           + string);
    }
}
