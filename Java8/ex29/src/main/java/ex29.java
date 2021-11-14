import utils.BigFraction;
import utils.HeapSort;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows how to combine the Java sequential streams and
 * completable futures framework to generate and reduce random big
 * fractions.  It also demonstrates the lazy processing of streams and
 * completable futures.
 */
@SuppressWarnings("unchecked")
public class ex29 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex29.class.getName();

    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    private static final int sMAX_FRACTIONS = 10;

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.
        new ex29().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        // Create a list of random big fractions that are unreduced.
        List<BigFraction> bigFractions = makeBigFractions();

        printList(bigFractions);

        print("Point 1: after printList()");

        // Obtain a future to a stream of reduced big fractions.
        Stream<CompletableFuture<BigFraction>> bigFractionStream =
            makeBigFractionStream(bigFractions);

        print("Point 2: after first map() in makeBigFractionStream()");

        CompletableFuture<List<BigFraction>> bigFractionsF =
            makeCompletableFutureStream(bigFractionStream);

        print("Point 3: after collect() in makeCompletableFutureStream");

        bigFractionsF
            // Sort the list in parallel and print the results.
            .thenCompose(this::sortAndPrintList)

            // Trigger all the processing and block until it's all done.
            .join();

        print("Point 4: after join()");
    }

    /**
     * @return Return a {@link List} of random unreduced {@link BigFraction} objects
     */
    private List<BigFraction> makeBigFractions() {
        return Stream
            // Generate an infinite number of random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))

            // Limit the number of BigFractions to sMAX_FRACTIONS.
            .limit(sMAX_FRACTIONS)

            // Trigger processing and collect into a list.
            .collect(toList());
    }

    /**
     * Return a {@link Stream} of {@link CompletableFuture} objects to
     * reduced {@link BigFraction} objects.
     *
     * @param bigFractions The {@link List} of {@link BigFraction} objects
     * @return A {@link Stream} of {@link CompletableFuture} objects to
     *         reduced {@link BigFraction} objects
     */
    private Stream<CompletableFuture<BigFraction>>
        makeBigFractionStream(List<BigFraction> bigFractions) {
        return bigFractions
            // Convert big fractions in list into a sequential stream.
            .stream()

            // Reduce big fractions asynchronously.
            .map(this::reduceBigFractionAsync);
    }

    /**
     * Returns a A {@link CompletableFuture} to a {@link List}
     *          of {@link BigFraction} objects
     * @param bigFractionStream A {@link Stream} of
     *                          {@link CompletableFuture<BigFraction>}
     *                          objects
     * @return A {@link CompletableFuture} to a {@link List}
     *         of {@link BigFraction} objects
     */
    private CompletableFuture<List<BigFraction>>
        makeCompletableFutureStream(Stream<CompletableFuture<BigFraction>>
                                    bigFractionStream) {
        // Convert the stream into an array of futures by calling
        // a terminal operation.
        CompletableFuture<BigFraction>[] futures =
            bigFractionStream.toArray(CompletableFuture[]::new);

        // Return a future to a stream of reduced big fractions.
        return CompletableFuture
            // Obtain a future that will complete when all futures in
            // bigFutureStream complete.
            .allOf(futures)

            // When all futures complete return a future to a stream
            // of joined big fractions.
            .thenApply(v -> Arrays
                       // Convert futures into a stream of futures.
                       .stream(futures)

                       // join() all futures and yield a stream of big
                       // fractions (join() never blocks).
                       .map(CompletableFuture::join)

                       // This call triggers all the processing above.
                       .collect(getCollectors()));
    }

    /**
     * A factory method that returns a large random BigFraction whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A large random BigFraction
     */
    private BigFraction makeBigFraction(Random random,
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
     * @return A future that when completed will yield the results of
     * reducing a big fractions asynchronously
     */
    private CompletableFuture<BigFraction>
        reduceBigFractionAsync(BigFraction bigFraction) {
        return CompletableFuture
            // Run this action in a common fork-join pool thread to
            // reduce the bigFraction.
            .supplyAsync(() -> {
                    BigFraction bf = BigFraction.reduce(bigFraction);
                    print("    reduceBigFractionAsync() = " + bf);
                    return bf;
                });
    }

    /**
     * @ return A collector that converts elements in a stream into a
     * list of big fractions
     */
    private Collector<BigFraction, ? , List<BigFraction>> getCollectors() {
        print("    getCollectors()");
        return Collectors.toList();
    }
        
    /**
     * Sort the {@code List} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuilder}
     * parameter.
     *      * @return A {@link CompletableFuture} to a sorted {@link List}
     *      *         of {@link BigFraction} objects
     */
    private CompletableFuture<Void> sortAndPrintList(List<BigFraction> list) {
        // This implementation uses quick sort to order the list.
        CompletableFuture<List<BigFraction>> quickSortF = CompletableFuture
            // Perform quick sort asynchronously.
            .supplyAsync(() -> quickSort(list));

        // This implementation uses heap sort to order the list.
        CompletableFuture<List<BigFraction>> heapSortF = CompletableFuture
            // Perform heap sort asynchronously.
            .supplyAsync(() -> heapSort(list));

        // Select the result of whichever sort implementation finishes
        // first and use it to print the sorted list.
        return quickSortF
            .acceptEither(heapSortF,
                          this::printList);
    }

    /**
     *
     */
    private void printList(List<BigFraction> sortedList) {
            // Print the results as mixed fractions.
            sortedList
                .forEach(fraction ->
                         print("     "
                               + fraction.toMixedString()));
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
     * Print the {@code string} together with thread information
     */
    private static void print(String string) {
        System.out.println("Thread["
                           + Thread.currentThread().getId()
                           + "]: "
                           + string);
    }
}
