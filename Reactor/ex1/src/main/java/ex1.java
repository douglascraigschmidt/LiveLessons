import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTester;
import utils.BigFraction;
import utils.ReactorUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.MonosCollector.toMono;

/**
 * This example shows how to reduce and/or multiply big fractions
 * using a wide range of features in the Reactor framework, including
 * flatMap(), collectList(), zipWith(), first(), when(), and
 * onErrorResume().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex1 {
    /**
     * Number of big fractions to process asynchronously in a Reactor
     * flux stream.
     */
    private static final int sMAX_FRACTIONS = 10;

    /**
     * These objects are used to pass params to various lambdas in the
     * test methods below.
     */
    private static final String sF1 = "62675744/15668936";
    private static final String sF2 = "609136/913704";
    private static final String sBI1 = "846122553600669882";
    private static final String sBI2 = "188027234133482196";

    /**
     * Represents a test that's completed running when it returns.
     */
    private static final Mono<Void> sVoidM =
        Mono.empty();

    /**
     * A big reduced fraction constant.
     */
    private static final BigFraction sBigReducedFraction =
        BigFraction.valueOf(new BigInteger("846122553600669882"),
                            new BigInteger("188027234133482196"),
                            true);

    /**
     * Stores a completed mono with a value of sBigReducedFraction.
     */
    private static final Mono<BigFraction> mBigReducedFractionM =
        Mono.just(sBigReducedFraction);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test synchronous BigFraction reduction using a mono and a
        // pipeline of operations that run on the calling thread.
        AsyncTester.register(ex1::testFractionReductionSync);

        // Test asynchronous BigFraction reduction using a Mono and a
        // pipeline of operations that run off the calling thread.
        AsyncTester.register(ex1::testFractionReductionAsync);

        // Test asynchronous BigFraction multiplication using a mono,
        // callable, and the common fork-join pool.
        AsyncTester.register(ex1::testFractionMultiplicationCallable);

        // Test asynchronous BigFraction multiplication and addition
        // using zipWith().
        AsyncTester.register(ex1::testFractionCombine);

        // Test BigFraction exception handling using mono methods.
        AsyncTester.register(ex1::testFractionExceptions1);

        // Test BigFraction multiplications using a stream of monos
        // and a pipeline of operations, including flatMap(),
        // collectList(), and first().
        AsyncTester.register(ex1::testFractionMultiplications1);

        // Test BigFraction multiplications by combining the Java
        // streams framework with the Reactor framework.
        AsyncTester.register(ex1::testFractionMultiplications2);

        @SuppressWarnings("ConstantConditions")
            long testCount = AsyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }

    /**
     * Test synchronous BigFraction reduction using a mono and a
     * pipeline of operations that run on the calling thread.
     */
    private static Mono<Void> testFractionReductionSync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction =
            BigFraction.valueOf(new BigInteger (sBI1),
                                new BigInteger(sBI2),
                                false);

        Callable<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction =
            BigFraction.reduce(unreducedFraction);

            sb.append("     unreducedFraction "
                      + unreducedFraction.toString()
                      + "\n     reduced improper fraction = "
                      + reducedFraction.toString());

            // Return the reduction.
            return reducedFraction;
        };

        Function<BigFraction, String> convertToMixedString = result -> {
            sb.append("\n     calling BigFraction::toMixedString\n");

            return result.toMixedString();
        };

        // Create a consumer to print the mixed reduced result.
        Consumer<String> printResult = result -> {
            sb.append("     mixed reduced fraction = " + result);
            display(sb.toString());
        };

        return Mono
            // Synchronously reduce the unreduced big fraction.
            .fromCallable(reduceFraction)

            // After big fraction is reduced return a mono to a
            // function that converts it to a mixed fraction string.
            .map(convertToMixedString)

            // Print result after it's converted to a mixed fraction.
            .doOnSuccess(printResult)

            // Return an empty mono.
            .then();
    }

    /**
     * Test asynchronous BigFraction reduction using a Mono and a
     * pipeline of operations that run off the calling thread.
     */
    private static Mono<Void> testFractionReductionAsync() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionReductionAsync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger (sBI1),
                                new BigInteger(sBI2),
                                false);

        Callable<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction =
            BigFraction.reduce(unreducedFraction);

            sb.append("     unreducedFraction "
                      + unreducedFraction.toString()
                      + "\n     reduced improper fraction = "
                      + reducedFraction.toString());

            // Return the reduction.
            return reducedFraction;
        };

        Function<BigFraction, String> convertToMixedString = result -> {
            sb.append("\n     calling BigFraction::toMixedString\n");

            return result.toMixedString();
        };

        // Create a consumer to print the mixed reduced result.
        Consumer<String> printResult = result -> {
            sb.append("     mixed reduced fraction = "
                      + result);
            display(sb.toString());
        };

        return ReactorUtils
            // Asynchronously reduce the unreduced big fraction.
            .fromCallableConcurrent(reduceFraction)

            // After big fraction is reduced return a mon to a
            // function that converts it a mixed fraction string.
            .map(convertToMixedString)

            // Print result after it's converted to a mixed fraction.
            .doOnSuccess(printResult)

            // Return an empty mono.
            .then();
    }

    /**
     * Test asynchronous BigFraction multiplication using a mono,
     * callable, and the common fork-join pool.
     */
    private static Mono<Void> testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(sF1);
            BigFraction bf2 = new BigFraction(sF2);

            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Submit the call to the common fork-join pool and store
        // the future it returns.
        Mono<BigFraction> mono = ReactorUtils
            .fromCallableConcurrent(call);

        // Block until the result is available.
        BigFraction result = mono.block();

        sb.append("     Callable.call() = "
                  + result.toMixedString());
        display(sb.toString());

        // Return an empty mono.
        return sVoidM;
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    private static Mono<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m1 = ReactorUtils
            .fromCallableConcurrent(() -> makeBigFraction(random, true)
                                    .multiply(sBigReducedFraction));

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m2 = ReactorUtils
            .fromCallableConcurrent(() -> makeBigFraction(random, true)
                                    .multiply(sBigReducedFraction));
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString());
            display(sb.toString());
        };

        return m1
            // Wait until m1 and m2 are complete and then add the
            // results.
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty mono.
            .then();
    }

    /**
     * Test BigFraction exception handling using Mono methods.
     */
    private static Mono<Void> testFractionExceptions1() {
        // Use StringBuffer to avoid race conditions.
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

        return Flux
            // Generate results both with and without exceptions.
            .just(true, false)

            // Iterate through the elements.
            .flatMap(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create/process a BigFraction asynchronously.
                    return ReactorUtils
                        .fromCallableConcurrent(() ->
                                                // May throw
                                                // ArithmeticException.
                                                BigFraction.valueOf(100, 
                                                                    denominator))

                        // Handle an exception.
                        .onErrorResume(t -> {
                                // If exception occurred return 0.
                                sb.append("\n     exception = " 
                                          + t.getMessage()
                                          + "\n");

                                // Convert error to 0.
                                return Mono.just(BigFraction.ZERO);
                            })

                        // Handle success.
                        .doOnSuccess(fraction -> {
                                // When mono completes multiply and
                                // store it in output.
                                fraction.multiply(sBigReducedFraction);
                                sb.append("     result = "
                                          + fraction.toMixedString());
                            });
                })

            // Convert the flux stream into a mono list.
            .collectList()

            // Display results when all processing is done.
            .flatMap(___ -> {
                    // Print results.
                    display(sb.toString());

                    // Return empty mono.
                    return sVoidM;
                });
    }

    /**
     * Test BigFraction multiplications using a stream of monos and a
     * pipeline of operations, including flatMap(), collectList(), and
     * first().
     */
    private static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // This async function reduces/multiplies a big fraction.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> ReactorUtils
                     // Multiply BigFractions asynchronously since it
                     // may run for a long time.
                     .fromCallableConcurrent(() -> reducedFraction
                                             .multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:");

        // Process the function in a flux stream.
        return ReactorUtils
            // Generate large, random, and unreduced fractions.
            .generate(() -> makeBigFraction(new Random(), false),
                      // Generate this many fractions.
                      sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(reduceAndMultiplyFraction)

            // Collect the results into a list.
            .collectList()

            // Process the results of the collected list.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Reactor framework.
     */
    private static Mono<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Function asynchronously reduces/multiplies a big fraction.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // Return a mono to a big fraction that's multiplied
            // asynchronously since it may run for a long time.
            .map(reducedFraction
                 -> reducedFraction.multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // mono to a list of big fractions that are being reduced
            // and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> sortAndPrintList(list,
                                              sb));
    }

    /**
     * A factory method that returns a large random BigFraction whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A large random BigFraction
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

        // Return a big fraction.
        return BigFraction.valueOf(numerator,
                                   denominator,
                                   reduced);
    }

    /**
     * Sort the {@code list} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuilder}
     * parameter.
     */
    private static Mono<Void> sortAndPrintList(List<BigFraction> list,
                                               StringBuilder sb) {
        // Quick sort the list asynchronously.
        Mono<List<BigFraction>> quickSortM = ReactorUtils
            .fromCallableConcurrent(() -> quickSort(list));

        // Merge sort the list asynchronously.
        Mono<List<BigFraction>> mergeSortM = ReactorUtils
            .fromCallableConcurrent(() -> mergeSort(list));

        return Mono
            // Select the result of whichever sort finishes first and
            // use it to print the sorted list.
            .first(quickSortM,
                   mergeSortM)

            // Process the first sorted list.
            .doOnSuccess(sortedList -> {
                              // Print the results as mixed fractions.
                              sortedList
                                  .forEach(fraction ->
                                           sb.append("\n     "
                                                     + fraction.toMixedString()));
                              display(sb.toString());
                          })
                
            // Return an empty mono.
            .then();
    }

    /**
     * Perform a quick sort on the {@code list}.
     */
    private static List<BigFraction> quickSort(List<BigFraction> list) {
        // Convert the list to an array.
        BigFraction[] bigFractionArray =
            list.toArray(new BigFraction[0]);

        // Order the array with quick sort.
        Arrays.sort(bigFractionArray);

        // Convert the array back to a list.
        return List.of(bigFractionArray);
    }

    /*
     * Perform a merge sort on the {@code list}.
     */
    private static List<BigFraction> mergeSort(List<BigFraction> list) {
        Collections.sort(list);
        return list;
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }
}
