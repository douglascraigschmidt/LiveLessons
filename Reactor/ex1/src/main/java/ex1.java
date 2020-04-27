import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import utils.AsyncTester;
import utils.BigFraction;
import utils.ReactorUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.MonosCollector.toMono;

/**
 * This example shows how to reduce and/or multiply big fractions
 * using a wide range of features in the Reactor framework.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex1 {
    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    private static final int sMAX_FRACTIONS = 10;

    /**
     * Represents a test that's already completed running when it
     * returns.
     */
    private static final CompletableFuture<Void> sCompleted =
        CompletableFuture.completedFuture(null);

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
     * Stores a completed future with a BigFraction value of
     * sBigReducedFraction.
     */
    private static final Mono<BigFraction> mBigReducedFractionM =
        Mono.just(sBigReducedFraction);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage "normal" (i.e., non-*Async())
        // methods.
        AsyncTester.register(ex1::testFractionReduction);

        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage *Async() methods.
        AsyncTester.register(ex1::testFractionReductionAsync);

        // Test BigFraction multiplication using a Callable, Future,
        // and the common fork-join pool.
        AsyncTester.register(ex1::testFractionMultiplicationCallable);

        // Test BigFraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and thenAccept()
        // completion stage method.
        AsyncTester.register(ex1::testFractionMultiplicationAsyncChaining);

        // Test big fraction multiplication and addition using a
        // supplyAsync() and thenCombine().
        AsyncTester.register(ex1::testFractionCombine);

        /*
        // Test BigFraction exception handling using
        // CompletableFutures and the handle() method.
        AsyncTester.register(ex1::testFractionExceptions1);

        // Test BigFraction exception handling using
        // CompletableFutures and the exceptionally() method.
        AsyncTester.register(ex1::testFractionExceptions2);

        // Test BigFraction exception handling using
        // CompletableFutures and the whenComplete() method.
        AsyncTester.register(ex1::testFractionExceptions3);
         */

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenCompose(), and acceptEither().
        AsyncTester.register(ex1::testFractionMultiplications1);
        
        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenComposeAsync(), and
        // acceptEither().
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
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage "normal" (i.e., non-*Async())
     * methods.
     */
    private static Mono<Void> testFractionReduction() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReduction()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction =
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
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
                // Asynchronously reduce the unreduced big fraction.
                .fromCallable(reduceFraction)

                // After the big fraction is reduced then return a future
                // to a computation that converts it into a string in
                // mixed fraction format.
                .map(convertToMixedString)

                // Print result after converting it to a mixed fraction.
                .doOnSuccess(printResult)
                .then();
    }

    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage *Async() methods.
     */
    private static Mono<Void> testFractionReductionAsync() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionReductionAsync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
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

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .map(convertToMixedString)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(printResult)
            .then();
    }

    /**
     * Test BigFraction multiplication using a Callable, Mono, and the
     * common fork-join pool.
     */
    private static Mono<Void> testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        // These "effectively final" objects are used to pass
        // params to the callable lambda below.
        String f1 = "62675744/15668936";
        String f2 = "609136/913704";

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(f1);
            BigFraction bf2 = new BigFraction(f2);

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

        return sVoidM;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * supplyAsync() factory method and thenAccept() completion stage
     * method.
     */
    private static Mono<Void> testFractionMultiplicationAsyncChaining() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionMultiplicationAsyncChaining()\n");

        // Create a supplier that multiplies two large fractions.
        Callable<BigFraction> fractionMultiplier = () -> {
            BigFraction bf1 = new BigFraction("62675744/15668936");
            BigFraction bf2 = new BigFraction("609136/913704");
                    
            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Create a consumer that print the result as a mixed fraction
        // after it's reduced.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction -> {
            sb.append("     Async chaining result = " 
                      + bigFraction.toMixedString());
            display(sb.toString());
        };

        return ReactorUtils
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .fromCallableConcurrent(fractionMultiplier)

            // This completion stage method is dispatched after the
            // BigFraction multiplication completes.
            .doOnSuccess(mixedFractionPrinter)
            .then();
    }

    /**
     * Test big fraction multiplication and addition using a
     * supplyAsync() and thenCombine().
     */
    private static Mono<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and multiply it.
        Mono<BigFraction> m1 = ReactorUtils
            .fromCallableConcurrent(() ->
                         // This code runs asynchronously.
                         makeBigFraction(random, false)
                         .multiply(sBigReducedFraction));

        // Create another random BigFraction and multiply it.
        Mono<BigFraction> m2 = ReactorUtils
            .fromCallableConcurrent(() ->
                         // This code runs asynchronously.
                         makeBigFraction(random, false)
                         .multiply(sBigReducedFraction));
        
        // Create a consumer that print the result as a mixed fraction
        // after it's reduced.
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
            .then();
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the handle() method.
     */
    private static CompletableFuture<Void> testFractionExceptions1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions1()\n");

        List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert list to a stream.
            .stream()

            // Iterate through the stream elements.
            .forEach(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Handle outcome of previous stage.
                        .handle((fraction, ex) -> {
                                // If exception occurred convert it to 0.
                                if (fraction == null) {
                                    sb.append("     exception = " + ex.getMessage());
                                    return BigFraction.ZERO;
                                } else
                                    // Multiply fraction by a constant.
                                    return fraction.multiply(sBigReducedFraction);
                            })

                        // When future completes prepare results for output.
                        .thenAccept(fraction ->
                                    sb.append("\n     result = "
                                              + fraction.toMixedString()));
                });

        // Print results.
        display(sb.toString());
        return sCompleted;
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the exceptionally() method.
     */
    private static CompletableFuture<Void> testFractionExceptions2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions2()\n");

        List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert list to a stream.
            .stream()

            // Iterate through the stream elements.
            .forEach(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Multiply fraction by a constant when
                        // previous stage completes.
                        .thenApply(fraction ->
                                   fraction.multiply(sBigReducedFraction))

                        // If exception occurred convert it to 0.
                        .exceptionally(ex -> {
                                sb.append("     exception = " + ex.getMessage());
                                return BigFraction.ZERO;
                            })

                        // When future completes prepare results for output.
                        .thenAccept(fraction ->
                                    sb.append("\n     result = "
                                              + fraction.toMixedString()));
                });

        // Print results.
        display(sb.toString());
        return sCompleted;
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the whenComplete() method.
     */
    private static CompletableFuture<Void> testFractionExceptions3() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions3()\n");

        List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert list to a stream.
            .stream()

            // Iterate through the stream elements.
            .forEach(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Multiply fraction by a constant when
                        // previous stage completes.
                        .thenApply(fraction ->
                                   fraction.multiply(sBigReducedFraction))

                        // When future completes prepare results for output,
                        // either normal or exceptional.
                        .whenComplete((fraction, ex) -> {
                                if (fraction != null)
                                    sb.append("\n     result = "
                                              + fraction.toMixedString());
                                else
                                    sb.append("     exception = " + ex.getMessage());
                            });
                });

        // Print results.
        display(sb.toString());
        return sCompleted;
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenCompose(), and acceptEither().
     */
    private static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // Lambda asynchronously reduces/multiplies a big fraction. 
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // thenCompose() is like flatMap(), i.e., it returns a
            // completable future to a multiplied big fraction.
            .flatMap(reducedFraction -> ReactorUtils
                     // Multiply BigFractions asynchronously since
                     // it may run for a long time.
                     .fromCallableConcurrent(()
                                             -> reducedFraction.multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:\n");

        // Process the two lambdas in a sequential stream.
        return ReactorUtils
            // Generate sMAX_FRACTIONS large and random fractions.
            .generate(() -> makeBigFraction(new Random(), false),
                      sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(reduceAndMultiplyFraction)

            // Collect the results into a list.
            .collectList()

            // Process the results of the collected list.
            .flatMap(list ->
                    // Sort and print the results after all
                    // asynchronous fraction reductions have
                    // completed.
                    sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenComposeAsync(), and
     * acceptEither().
     */
    private static Mono<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Lambda asynchronously reduces/multiplies a big fraction. 
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // thenApplyAsync() returns a completable future to a big
            // fraction that's multiplied asynchronously since it may
            // run for a long time.
            .map(reducedFraction
                 -> reducedFraction.multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:\n");

        // Process the two lambdas in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // future to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> sortAndPrintList(list,
                                              sb));
    }

    /**
     * Sort the {@code list} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuilder}
     * parameter.
     */
    private static Mono<Void> sortAndPrintList(List<BigFraction> list,
                                               StringBuilder sb) {
        // This implementation uses quick sort to order the list.
        Mono<List<BigFraction>> quickSortFuture = ReactorUtils
            // Perform quick sort asynchronously.
            .fromCallableConcurrent(() -> quickSort(list));

        // This implementation uses merge sort to order the list.
        Mono<List<BigFraction>> mergeSortFuture = ReactorUtils
            // Perform merge sort asynchronously.
            .fromCallableConcurrent(() -> mergeSort(list));

        return Mono
            // Select the result of whichever sort implementation
            // finishes first and use it to print the sorted list.
            .first(quickSortFuture,
                   mergeSortFuture)

            // Process the first sorted list.
            .doOnSuccess(sortedList -> {
                              // Print the results as mixed fractions.
                              sortedList
                                  .forEach(fraction ->
                                           sb.append("     "
                                                     + fraction.toMixedString()
                                                     + "\n"));
                              display(sb.toString());
                          })
            .then();
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
     * A factory method that returns a large random BigFraction whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A completable future to a large random BigFraction
     */
    private static Mono<BigFraction>
        makeBigFractionAsync(Random random,
                             boolean reduced) {
        return ReactorUtils
            .fromCallableConcurrent(() -> {
                    // Create a large random big integer.
                    BigInteger numerator =
                        new BigInteger(150000, random);

                    // Create a denominator that's between 1 to 10
                    // times smaller than the numerator.
                    BigInteger denominator =
                        numerator.divide(BigInteger
                                         .valueOf(random
                                                  .nextInt(10) + 1));

                    // Return a big fraction.
                    return BigFraction.valueOf(numerator,
                                               denominator,
                                               reduced);
                });
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
