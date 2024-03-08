package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A utility class containing helpful methods for manipulating various
 * BigFraction features.
 */
@SuppressWarnings("ALL")
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
     * Stores a completed single with a value of sBigReducedFraction.
     */
    public static final Single<BigFraction> mBigReducedFractionM =
            Single.just(sBigReducedFraction);

    /**
     * Represents a test that's completed running when it returns.
     */
    public static final Completable sVoidS =
        Completable.complete();

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
     * Sort the {@link List} in parallel using quicksort and mergesort
     * and then store/print the results in the {@link StringBuffer}
     * parameter.
     */
    public static Completable sortAndPrintList(List<BigFraction> list,
                                               StringBuffer sb) {
        // Quick sort the list asynchronously.
        Single<List<BigFraction>> quickSortS = Single
            // Obtain the results of quick sorting the list.
            .fromCallable(() -> quickSort(list))

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.computation());

        // Heap sort the list asynchronously.
        Single<List<BigFraction>> heapSortS =  Single
            // Obtain the results of heap sorting the list.
            .fromCallable(() -> heapSort(list))

            // Run all the processing in the parallel thread pool.
            .subscribeOn(Schedulers.computation());

        return Single
            // Select the result of whichever sort finishes first and
            // use it to print the sorted list.
            .ambArray(quickSortS, heapSortS)

            // Use flatMapCompletable() to display first sorted list.
            .flatMapCompletable(sortedList -> printList(sortedList, sb));
    }

    /**
     * Store/print the results of the {@link List} in the {@link
     * StringBuffer} parameter.
     */
    public static Completable printList(List<BigFraction> list,
                                        StringBuffer sb) {
        // Display the results as mixed fractions.
        Consumer<List<BigFraction>> displayList = l -> {
            // Iterate through each BigFraction in the list.
            l.forEach(fraction ->
                      sb.append("\n     "
                                + fraction.toMixedString()));
            sb.append("\n");
            display(sb.toString());
        };

        return Single
            // Convert list into a Single.
            .just(list)

            // Display the list.
            .doOnSuccess(displayList)
                
            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * Perform a quick sort on the {@code list}.
     */
    public static List<BigFraction> quickSort(List<BigFraction> list) {
        List<BigFraction> copy = new ArrayList<>(list);
    
        // Order the list with quick sort.
        Collections.sort(copy);

        return copy;
    }

    /*
     * Perform a heap sort on the {@code list}.
     */
    public static List<BigFraction> heapSort(List<BigFraction> list) {
        List<BigFraction> copy = new ArrayList<>(list);

        // Order the list with heap sort.
        HeapSort.sort(copy);

        return copy;
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
     * Convert {@code unreducedFraction} to a mixed string and {@code
     * reducedFraction} to a mixed string and append it to the
     * contents of {@code stringBuilder}.
     */
    public static void logBigFraction(BigFraction unreducedFraction,
                                      BigFraction reducedFraction,
                                      StringBuffer sb) {
        sb.append("     "
                  + unreducedFraction.toMixedString()
                  + " x "
                  + reducedFraction.toMixedString()
                  + "\n");
    }

    /**
     * Convert {@code unreducedFraction} to a mixed string and {@code
     * reducedFraction} to a mixed string and append it to the
     * contents of {@code stringBuilder}.
     */
    public static void logBigFraction(BigFraction unreducedFraction,
                                      BigFraction reducedFraction,
                                      StringBuilder sb) {
        sb.append("     "
                + unreducedFraction.toMixedString()
                + " x "
                + reducedFraction.toMixedString()
                + "\n");
    }

    /**
     * Convert {@code bigFraction} to a mixed string, {@code
     * reducedFraction} to a mixed string, and {@code result} to a
     * mixed string and append it to the contents of {@code
     * stringBuilder}.
     */
    public static void logBigFractionResult(BigFraction bigFraction,
                                            BigFraction reducedFraction,
                                            BigFraction result,
                                            StringBuffer sb) {
        sb.append("     "
                  + bigFraction.toMixedString()
                  + " x "
                  + reducedFraction.toMixedString()
                  + " = "
                  + result.toMixedString()
                  + "\n");
    }

    /**
     * Display {@code bigFraction} and the contents of {@code stringBuffer}.
     */
    public static void displayMixedBigFraction(BigFraction bigFraction,
                                               StringBuffer stringBuffer) {
        stringBuffer.append("     Mixed BigFraction result = "
                            + bigFraction
                            + "\n");
        BigFractionUtils.display(stringBuffer.toString());
    }

    /**
     * Iterate through the contents of the {@link Map} param and then
     * store and print the results in the {@link StringBuffer}
     * parameter.
     */
    public static Completable printMap(Map<BigInteger, Collection<BigInteger>> map,
                                       StringBuffer sb) {
        // Display the results as mixed fractions.
        Consumer<Map<BigInteger, Collection<BigInteger>>> displayMap = ___ -> {
            // Iterate through each BigFraction in the map.
            map.forEach((numerator, denominators) ->
                        denominators
                        .forEach(denominator -> sb
                                 .append("\n     "
                                         + BigFraction
                                         .valueOf(numerator,
                                                  denominator,
                                                  true)
                                         .toMixedString()
                                         + " ")));
            sb.append("\n");
            display(sb.toString());
        };

        return Single
            // Create a Single that emits the map.
            .just(map)

            // Use doOnSuccess() to display the map.
            .doOnSuccess(displayMap)
                
            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
