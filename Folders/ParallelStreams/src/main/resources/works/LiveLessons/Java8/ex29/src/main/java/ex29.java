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
 * completable futures frameworks to generate and reduce random {@link
 * BigFraction} objects concurrently and asynchronously.  It also shows
 * the lazy processing semantics of the Java streams and completable
 * futures frameworks.
 */
@SuppressWarnings("unchecked")
public class ex29 {
    /**
     * Number of {@link BigFraction} objects to process asynchronously
     * in a stream.
     */
    private static final int sMAX_FRACTIONS = 10;

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        print("Point 0: starting up");

        // Create a List of random BigFractions that are unreduced.
        List<BigFraction> bigFractions = makeBigFractions();

        printList(bigFractions);

        print("Point 1: after printList()");

        // Obtain a future to a stream of reducing BigFractions.
        Stream<CompletableFuture<BigFraction>> bigFractionStream =
            makeBigFractionStream(bigFractions);

        print("Point 2: after first map() in makeBigFractionStream()");

        // Obtain a future to a list of reduced BigFractions when they complete.
        CompletableFuture<List<BigFraction>> bigFractionsF =
            makeCompletableFutureStream(bigFractionStream);

        print("Point 3: after collect() in makeCompletableFutureStream()");

        CompletableFuture<Void> results = bigFractionsF
                // Sort the List in parallel and print the results.
                .thenCompose(ex29::sortAndPrintListAsync);

        print("Point 4: after thenCompose()");

        // Trigger all the processing and block until it's all done.
        results.join();

        print("Point 5: after join()");
    }

    /**
     * @return Return a {@link List} of random unreduced {@link BigFraction}
     *         objects
     */
    private static List<BigFraction> makeBigFractions() {
        return Stream
            // Generate an infinite number of random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))

            // Limit the number of BigFractions to sMAX_FRACTIONS.
            .limit(sMAX_FRACTIONS)

            // Trigger processing and collect into a List.
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
    private static Stream<CompletableFuture<BigFraction>>
        makeBigFractionStream(List<BigFraction> bigFractions) {
        return bigFractions
            // Convert BigFractions in List into a sequential stream.
            .stream()

            // Reduce BigFractions asynchronously.
            .map(ex29::reduceBigFractionAsync);
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
    private static CompletableFuture<List<BigFraction>>
        makeCompletableFutureStream(Stream<CompletableFuture<BigFraction>>
                                    bigFractionStream) {
        // Convert the stream into an array of futures by calling
        // the toArray() terminal operation.
        CompletableFuture<BigFraction>[] futures =
            bigFractionStream.toArray(CompletableFuture[]::new);

        // Return a future to a stream of reduced BigFractions.
        return CompletableFuture
            // Obtain a future that will complete when all futures in
            // bigFutureStream complete.
            .allOf(futures)

            // When all futures complete return a future to a stream
            // of joined BigFractions.
            .thenApply(v -> Stream
                       // Convert futures array into a stream of futures.
                       .of(futures)

                       // join() all futures and yield a stream of big
                       // fractions (join() never blocks).
                       .map(CompletableFuture::join)

                       // This call triggers all the processing above.
                       .collect(getListCollector()));
    }

    /**
     * A factory method that returns a large random {@link BigFraction} whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A large random {@link BigFraction}
     */
    private static BigFraction makeBigFraction(Random random,
                                               boolean reduced) {
        // Create a large random big integer.
        BigInteger numerator =
            new BigInteger(150000, random);

        // Create a denominator that's between 1 to 10 times smaller
        // than the numerator.
        BigInteger denominator =
            numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

        // Return a BigFraction.
        return BigFraction.valueOf(numerator,
                                   denominator,
                                   reduced);
    }

    /**
     * @return A {@link CompletableFuture} that when completed yields
     * the results of reducing a {@link BigFraction} asynchronously
     */
    private static CompletableFuture<BigFraction>
        reduceBigFractionAsync(BigFraction bigFraction) {
        return CompletableFuture
            // Arrange to run this action in a common fork-join pool thread
            // to reduce the bigFraction.
            .supplyAsync(() -> {
                    BigFraction rbf = BigFraction.reduce(bigFraction);
                    print("    reduceBigFractionAsync() = " + rbf);
                    return rbf;
                });
    }

    /**
     * @return A {@link Collector} that converts elements in a stream
     *         into a {@link List} of {@link BigFraction} objects
     */
    private static Collector<BigFraction, ? , List<BigFraction>>
        getListCollector() {
        print("    getListCollector()");
        return Collectors.toList();
    }
        
    /**
     * Sort the {@link List} in parallel using quicksort and heapsort
     * and then store the results in the {@link StringBuilder}
     * parameter.
     *
     * @return A {@link CompletableFuture} to a sorted {@link List} of
     *         {@link BigFraction} objects
     */
    private static CompletableFuture<Void> sortAndPrintListAsync
        (List<BigFraction> list) {
        // This implementation uses quick sort to order list.
        CompletableFuture<List<BigFraction>> quickSortF = CompletableFuture
            // Perform quick sort asynchronously.
            .supplyAsync(() -> quickSort(list));

        // This implementation uses heap sort to order list.
        CompletableFuture<List<BigFraction>> heapSortF = CompletableFuture
            // Perform heap sort asynchronously.
            .supplyAsync(() -> heapSort(list));

        // Select the result of whichever sort implementation finishes
        // first and use it to print the sorted list.
        return quickSortF
            .acceptEither(heapSortF,
                          ex29::printList);
    }

    /**
     * Print the contents of the {@link List}.
     *
     * @param list The {@link List}
     */
    private static void printList(List<BigFraction> list) {
        // Print the results as mixed fractions.
        list
            .forEach(fraction ->
                     print("     "
                           + fraction.toMixedString()));
    }

    /**
     * Perform a quick sort on the {@link List}.
     *
     * @return A sorted {@link List}
     */
    private static List<BigFraction> quickSort(List<BigFraction> list) {
        List<BigFraction> copy = new ArrayList<>(list);
    
        // Order the list with quick sort.
        Collections.sort(copy);

        return copy;
    }

    /*
     * Perform a heap sort on the {@link List}.
     *
     * @return A sorted {@link List}
     */
    private static List<BigFraction> heapSort(List<BigFraction> list) {
        List<BigFraction> copy = new ArrayList<>(list);

        // Order the list with heap sort.
        HeapSort.sort(copy);

        return copy;
    }

    /**
     * Print the {@link String} together with thread information.
     */
    private static void print(String string) {
        System.out.println("Thread["
                           + Thread.currentThread().getId()
                           + "]: "
                           + string);
    }
}
