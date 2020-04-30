import utils.AsyncTester;
import utils.BigFraction;
import utils.HeapSort;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static utils.FuturesCollector.toFuture;

/**
 * This example shows how to reduce and/or multiply big fractions
 * using a wide range of features in the Java completable futures
 * framework, including many factory methods, completion stage
 * methods, arbitrary-arity methods, and exception handling methods.
 */
public class ex8 {
    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    private static int sMAX_FRACTIONS = 10;

    /**
     * These final strings are used to pass params to various lambdas
     * in the test methods below.
     */
    private static final String sF1 = "62675744/15668936";
    private static final String sF2 = "609136/913704";
    private static final String sBI1 = "846122553600669882";
    private static final String sBI2 = "188027234133482196";

    /**
     * Represents a test that's already completed running when it
     * returns.
     */
    private static final CompletableFuture<Void> sCompleted =
        CompletableFuture.completedFuture(null);

    /**
     * A big reduced fraction constant.
     */
    private static final BigFraction sBigReducedFraction =
        BigFraction.valueOf(new BigInteger(sBI1),
                            new BigInteger(sBI2),
                            true);

    /**
     * Stores a completed future with a BigFraction value of
     * sBigReducedFraction.
     */
    private static final CompletableFuture<BigFraction> mBigReducedFractionFuture =
        CompletableFuture.completedFuture(sBigReducedFraction);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage "normal" (i.e., non-*Async())
        // methods.
        AsyncTester.register(ex8::testFractionReduction);

        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage *Async() methods.
        AsyncTester.register(ex8::testFractionReductionAsync);

        // Test the use of a BigFraction constant using basic features
        // of a CompletableFuture and an explicit Java Thread.
        AsyncTester.register(ex8::testFractionConstantThread);

        // Test BigFraction multiplication using basic features of
        // CompletableFuture and an explicit Java Thread.
        AsyncTester.register(ex8::testFractionMultiplicationThread);

        // Test BigFraction multiplication using a CompletableFuture and
        // its runAsync() and join() methods.
        AsyncTester.register(ex8::testFractionMultiplicationRunAsync);

        // Test BigFraction multiplication using a Callable, Future,
        // and the common fork-join pool.
        AsyncTester.register(ex8::testFractionMultiplicationCallable);

        // Test BigFraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and join() method.
        AsyncTester.register(ex8::testFractionMultiplicationSupplyAsync);

        // Test BigFraction multiplication using a CompletableFuture and
        // its completeAsync() factory method and join() method.
        AsyncTester.register(ex8::testFractionMultiplicationCompleteAsync);

        // Test big fraction multiplication and addition using a
        // supplyAsync() and thenCombine().
        AsyncTester.register(ex8::testFractionCombine);

        // Test BigFraction exception handling using
        // CompletableFutures and the handle() method.
        AsyncTester.register(ex8::testFractionExceptions1);
        
        // Test BigFraction exception handling using
        // CompletableFutures and the exceptionally() method.
        AsyncTester.register(ex8::testFractionExceptions2);

        // Test BigFraction exception handling using
        // CompletableFutures and the whenComplete() method.
        AsyncTester.register(ex8::testFractionExceptions3);

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenCompose(), and acceptEither().
        AsyncTester.register(ex8::testFractionMultiplications1);

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenComposeAsync(), and
        // acceptEither().
        AsyncTester.register(ex8::testFractionMultiplications2);

        AsyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();
    }

    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage "normal" (i.e., non-*Async())
     * methods.
     */
    private static CompletableFuture<Void> testFractionReduction() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReduction()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction =
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
                                false);

        Supplier<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction = BigFraction
                    .reduce(unreducedFraction);

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

        return CompletableFuture
            // Asynchronously reduce the unreduced big fraction.
            .supplyAsync(reduceFraction)

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .thenApply(convertToMixedString)

            // Print result after converting it to a mixed fraction.
            .thenAccept(printResult);
    }

    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage *Async() methods.
     */
    private static CompletableFuture<Void> testFractionReductionAsync() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionReductionAsync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger(sBI1),
                                new BigInteger(sBI2),
                                false);

        Supplier<BigFraction> reduceFraction = () -> {
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

        return CompletableFuture
            // Asynchronously reduce the unreduced big fraction.
            .supplyAsync(reduceFraction)

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .thenApplyAsync(convertToMixedString)

            // Print result after converting it to a mixed fraction.
            .thenAcceptAsync(printResult);
    }

    /**
     * Test the use of a BigFraction constant using basic features of
     * a CompletableFuture and an explicit Java Thread.
     */
    private static CompletableFuture<Void> testFractionConstantThread() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionConstantThread()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda 
        // sets the future to a constant.
        new Thread (() -> {
                // Set future to a constant.
                future.complete(mBigReducedFractionFuture.join());
        }).start();

        // Print the result, blocking until it's ready.
        sb.append("     Thread result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using basic features of a
     * CompletableFuture and an explicit Java Thread.
     */
    private static CompletableFuture<Void> testFractionMultiplicationThread() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationThread()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda multiplies
        // two large fractions.
        new Thread (() -> {
                BigFraction bf1 =
                    new BigFraction(sF1);
                BigFraction bf2 =
                    new BigFraction(sF2);
                    
                // Complete the future once the computation is
                // finished.
                future.complete(bf1.multiply(bf2));
        }).start();

        // Print the result, blocking until it's ready.
        sb.append("     Thread result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * runAsync() and join() methods.
     */
    private static CompletableFuture<Void> testFractionMultiplicationRunAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationRunAsync()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .runAsync(() -> {
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);
                    
                    // Complete the future once the computation is
                    // finished.
                    future.complete(bf1.multiply(bf2));
                });

        // Print the result, blocking until it's ready.
        sb.append("     runAsync() result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a Callable, Future, and
     * the common fork-join pool.
     */
    private static CompletableFuture<Void> testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        try {
            // Create a callable that multiplies two large fractions.
            Callable<BigFraction> call = () -> {
                BigFraction bf1 = new BigFraction(sF1);
                BigFraction bf2 = new BigFraction(sF2);

                // Return the result of multiplying the fractions.
                return bf1.multiply(bf2);
            };

            // Submit the call to the common fork-join pool and store
            // the future it returns.
            Future<BigFraction> future =
                ForkJoinPool.commonPool().submit(call);

            // Block until the result is available.
            BigFraction result = future.get();

            sb.append("     Callable.call() = "
                      + result.toMixedString());
            display(sb.toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and
     * its supplyAsync() factory method and join() method.
     */
    private static CompletableFuture<Void> testFractionMultiplicationSupplyAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSupplyAsync()\n");

        // Create a future that completes when the supplier submitted
        // to the common fork-join pool completes.
        CompletableFuture<BigFraction> future = CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .supplyAsync(() -> {
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);
                    
                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });

        // Print the result, blocking until it's ready.
        sb.append("     supplyAsync() result = " 
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and
     * its completeAsync() factory method and join() method.
     */
    private static CompletableFuture<Void> testFractionMultiplicationCompleteAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCompleteAsync()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Register an action that appends a string when run.
        future
            .thenRun(() -> sb.append("     completeAsync() result = "));

        future
            // Complete this future with the result of multiplying two
            // large fractions together.
            .completeAsync(() -> {
                    // Multiply two large fractions.
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);

                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });

        // Append the result, blocking until it's ready.
        sb.append(future.join().toMixedString());

        // Print the result.
        display(sb.toString());
        return sCompleted;
    }

    /**
     * Test big fraction multiplication and addition using a
     * supplyAsync() and thenCombine().
     */
    private static CompletableFuture<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf1 = CompletableFuture
            // This code runs asynchronously.
            .supplyAsync(() -> makeBigFraction(random, true)
                         // Multiply a random fraction with a constant
                         // fraction.
                         .multiply(sBigReducedFraction));

        // Create another random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf2 = CompletableFuture
            // This code runs asynchronously.
            .supplyAsync(() -> makeBigFraction(random, true)
                         // Multiply a random fraction with a constant
                         // fraction.
                         .multiply(sBigReducedFraction));
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's reduced.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString());
            display(sb.toString());
        };

        return cf1
            // Wait until cf1 and cf2 are complete and then add the
            // results.
            .thenCombine(cf2,
                         BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .thenAccept(mixedFractionPrinter);
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the handle() method.
     */
    private static CompletableFuture<Void> testFractionExceptions1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

        return List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert to a stream.
            .stream()

            // Iterate through the elements.
            .map(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
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
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the exceptionally() method.
     */
    private static CompletableFuture<Void> testFractionExceptions2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions2()\n");

        return List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert to stream.
            .stream()

            // Iterate through the elements.
            .map(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
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
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the whenComplete() method.
     */
    private static CompletableFuture<Void> testFractionExceptions3() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions3()\n");

        return List
            // Generate results both with and without exceptions.
            .of(true, false)

            // Convert to a stream.
            .stream()

            // Handle both true and false elements.
            .map(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Multiply fraction by a constant when
                        // previous stage completes.
                        .thenApply(fraction ->
                                   fraction.multiply(sBigReducedFraction))

                        // When future completes prepare results for
                        // output, either normal or exceptional.
                        .whenComplete((fraction, ex) -> {
                                if (fraction != null)
                                    sb.append("\n     result = "
                                              + fraction.toMixedString());
                                else // if (ex != null)
                                    sb.append("     exception = " + ex.getMessage());
                            })

                        // Swallow the exception.
                        .exceptionally((e) -> BigFraction.ZERO);
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenCompose(), and acceptEither().
     */
    private static CompletableFuture<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // Lambda asynchronously reduces/multiplies a big fraction. 
        Function<BigFraction, 
                 CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(unreducedFraction))

            // thenCompose() is like flatMap(), i.e., it returns a
            // completable future to a multiplied big fraction.
            .thenCompose(reducedFraction -> CompletableFuture
                         // Multiply BigFractions asynchronously since
                         // it may run for a long time.
                         .supplyAsync(() -> reducedFraction
                                      .multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:");

        // Process reduceAndMultiplyFraction in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // future to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toFuture())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .thenCompose(list -> sortAndPrintList(list,
                                                  sb));
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenComposeAsync(), and
     * acceptEither().
     */
    private static CompletableFuture<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Function asynchronously reduces/multiplies a big fraction.
        Function<BigFraction,
            CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(unreducedFraction))

            // thenApplyAsync() returns a completable future to a big
            // fraction that's multiplied asynchronously since it may
            // run for a long time.
            .thenApplyAsync(reducedFraction -> reducedFraction
                            .multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:");

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
            .collect(toFuture())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .thenCompose(list -> sortAndPrintList(list,
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
    private static CompletableFuture<Void> sortAndPrintList(List<BigFraction> list,
                                                            StringBuilder sb) {
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
                          sortedList -> {
                              // Print the results as mixed fractions.
                              sortedList
                                  .forEach(fraction ->
                                           sb.append("\n     "
                                                     + fraction.toMixedString()));
                              display(sb.toString());
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
     * Perform a heap sort on the {@code list}.
     */
    private static List<BigFraction> heapSort(List<BigFraction> list) {
        // Convert the list to an array.
        BigFraction[] bigFractionArray =
            list.toArray(new BigFraction[0]);

        // Order the array with heap sort.
        HeapSort.sort(bigFractionArray);

        // Convert the array back to a list.
        return List.of(bigFractionArray);
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
