import utils.BigFraction;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows how to combine the Java sequential streams and
 * completable futures framework to generate and reduce random big
 * fractions.  It also demonstrates the lazy processing of streams.
 */
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
        List<BigFraction> bigFractions = Stream
                // Generate sMAX_FRACTIONS random unreduced BigFractions.
                .generate(() -> makeBigFraction(new Random(), false))
                .limit(sMAX_FRACTIONS)

                // Trigger processing and collect into a list.
                .collect(toList());

        // Obtain a future to a stream of reduced big fractions.
        Stream<CompletableFuture<BigFraction>> bigFractionStream = bigFractions
                // Convert big fractions in list into a sequential stream.
                .stream()

                // Reduce big fractions asynchronously.
                .map(this::reduceBigFractionAsync);

        print("Point 1: after first map()");

        // Convert the stream into an array of futures.
        CompletableFuture<BigFraction>[] futures =
                bigFractionStream.toArray(CompletableFuture[]::new);

        // Create a future to a stream of reduced big fractions.
        CompletableFuture<Stream<BigFraction>> streamF = CompletableFuture
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
                        .map(CompletableFuture::join));

        print("Point 2: after second map()");

        streamF
                // Collect the big fractions into a list.
                .thenApply(stream -> stream
                        // This call triggers all the processing above.
                        .collect(getCollectors()))

                // Sort the list in parallel.
                .thenCompose(this::sortList)

                // Print the list.
                .thenAccept(this::printList)

                // Block until all the processing is done.
                .join();

        print("Point 3: after join()");
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
    private CompletableFuture<BigFraction> reduceBigFractionAsync(BigFraction bigFraction) {
        return CompletableFuture
            // Run this action in a common fork-join pool thread to
            // reduce the big fraction.
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
    private Collector<BigFraction,?, List<BigFraction>> getCollectors() {
        print("    getCollectors()");
        return Collectors.toList();
    }

    /**
     * @return the sorted {@code list}
     */
    private CompletableFuture<List<BigFraction>> sortList(List<BigFraction> list) {
        // This implementation uses quick sort to order the list.
        CompletableFuture<List<BigFraction>> quickSortF = CompletableFuture
            // Perform quick sort asynchronously.
            .supplyAsync(() -> quickSort(list));

        // This implementation uses merge sort to order the list.
        CompletableFuture<List<BigFraction>> mergeSortF = CompletableFuture
            // Perform merge sort asynchronously.
            .supplyAsync(() -> mergeSort(list));

        // Return result of whichever sort implementation finishes first.
        return quickSortF
            .applyToEither(mergeSortF,
                           Function.identity());
    }

    /**
     * Perform a quick sort on the {@code list}
     */
    private List<BigFraction> quickSort(List<BigFraction> list) {
        // Convert the list to an array.
        BigFraction[] bigFractionArray =
            list.toArray(new BigFraction[0]);

        // Order the array with quick sort.
        Arrays.sort(bigFractionArray);

        // Convert the array back to a list.
        return List.of(bigFractionArray);
    }

    /*
     * Perform a merge sort on the {@code list}
     */
    private List<BigFraction> mergeSort(List<BigFraction> list) {
        Collections.sort(list);
        return list;
    }

    /**
     * Print the {@code sortedList}
     */
    private void printList(List<BigFraction> sortedList) {
        // Print the results as mixed fractions.
        sortedList
            .forEach(fraction ->
                     print("     "
                           + fraction.toMixedString()));
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
