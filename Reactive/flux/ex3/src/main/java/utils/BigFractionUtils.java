package utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

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
     * Represents a test that's completed running when it returns.
     */
    public static final Mono<Void> sVoidM =
            Mono.empty();

    /**
     * A big reduced fraction constant.
     */
    public static final BigFraction sBigReducedFraction =
            BigFraction.valueOf(new BigInteger(sBI1),
                    new BigInteger(sBI2),
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
     * Sort the {@link List} in parallel using quicksort and heapsort
     * and then store and print the results in the {@link
     * StringBuffer} parameter.
     */
    public static Mono<Void> sortAndPrintList(List<BigFraction> list,
                                              StringBuffer sb) {
        // Quicksort the list asynchronously.
        var quickSortM = Mono
            // Use the fromCallable() factory method to obtain the
            // results of quick sorting the list.
            .fromCallable(() -> quickSort(list))

            // Use subscribeOn() to run all the processing in the
            // parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        // Heapsort the list asynchronously.
        var heapSortM =  Mono
            // Use the fromCallable() factory method to obtain the
            // results of heap sorting the list.
            .fromCallable(() -> heapSort(list))

            // Use subscribeOn() to run all the processing in the
            // parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        // Display the results as mixed fractions.
        Consumer<List<BigFraction>> displayList = sortedList -> {
            // Iterate through each BigFraction in the sorted list.
            sortedList.forEach(fraction ->
                               sb.append("\n     "
                                         + fraction.toMixedString()));
            sb.append("\n");
            display(sb.toString());
        };

        return Mono
            // Use firstWithSignal() to select the result of whichever
            // sort finishes first and use it to print the sorted
            // list.
            .firstWithSignal(quickSortM,
                             heapSortM)

            // Use doOnSuccess() to display the first sorted list.
            .doOnSuccess(displayList)
                
            // Use then() to return an empty mono to synchronize with
            // the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Iterate through the contents of the {@link Map} param and then
     * store and print the results in the {@link StringBuffer}
     * parameter.
     */
    public static Mono<Void> printMap(Map<BigInteger, Collection<BigInteger>> map,
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

        return Mono
            // Create a Mono that emits the map.
            .just(map)

            // Use doOnSuccess() to display the map.
            .doOnSuccess(displayMap)
                
            // Use then() to return an empty mono to synchronize with
            // the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Perform a quick sort on the {@code list}.
     */
    public static List<BigFraction> quickSort(List<BigFraction> list) {
        var copy = new ArrayList<>(list);
    
        // Order the list with quick sort.
        Collections.sort(copy);

        return copy;
    }

    /*
     * Perform a heap sort on the {@code list}.
     */
    public static List<BigFraction> heapSort(List<BigFraction> list) {
        var copy = new ArrayList<>(list);

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
}
