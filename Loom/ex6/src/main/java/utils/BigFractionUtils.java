package utils;

import common.Options;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class BigFractionUtils {
    /**
     * These final strings are used to pass params to various lambdas
     * in the test methods below.
     */
    public static final String sBI1 = "846122553600669882";
    public static final String sBI2 = "188027234133482196";

    /**
     * Define a reduced {@link BigFraction} constant.
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
     * @param reduced A flag indicating whether to reduce the fraction or not
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
     * Generate a {@link List} of random {@link BigFraction} objects.
     */
    public static List<BigFraction> generateRandomBigFractions(int count, 
                                                               boolean reduced) {
        // Generate a List of random BigFraction objects.
        return Stream
            // Generate 'count' random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), reduced))

            .limit(count)
                   
            // Trigger intermediate operations and collect into a List.
            .toList();
    }

    /**
     * Sort the {@code List} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuilder}
     * parameter.
     */
    public static CompletableFuture<Void> sortAndPrintList
        (List<BigFraction> list,
         StringBuffer sb) {
        // This implementation uses quick sort to order the list.
        CompletableFuture<List<BigFraction>> quickSortF = CompletableFutureEx
            // Perform quick sort asynchronously.
            .supplyAsync(() -> quickSort(list));

        // This implementation uses heap sort to order the list.
        CompletableFuture<List<BigFraction>> heapSortF = CompletableFutureEx
            // Perform heap sort asynchronously.
            .supplyAsync(() -> heapSort(list));

        // Select the result of whichever sort implementation finishes
        // first and use it to print the sorted list.
        return quickSortF
            .acceptEither(heapSortF,
                          sortedList -> {
                              if (Options.instance().getDiagnosticsEnabled()) {
                                  // Print the results as mixed fractions.
                                  sortedList
                                          .forEach(fraction ->
                                                  sb.append("\n     "
                                                          + fraction
                                                          .toMixedString()));

                                  display(sb.toString());
                              }
                          });
    }

    /**
     * Perform a quick sort on the {@code list}.
     */
    private static List<BigFraction> quickSort(List<BigFraction> list) {
        List<BigFraction> copy = new ArrayList<>(list);
    
        // Order the list with quick sort.
        Collections.sort(copy);

        return copy;
    }

    /*
     * Perform a heap sort on the {@code list}.
     */
    private static List<BigFraction> heapSort(List<BigFraction> list) {
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
                           + Thread.currentThread().threadId()
                           + "] "
                           + string);
    }
}
