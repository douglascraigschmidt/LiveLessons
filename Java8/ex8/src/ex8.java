import utils.BigFraction;
import utils.FuturesCollector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.FuturesCollector.toFuture;

/**
 * This example shows how to reduce and/or multiply big fractions
 * using a wide range of CompletableFuture features, including many
 * factory methods, completion stage methods, arbitrary-arity methods,
 * and exception handling methods.
 */
public class ex8 {
    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    private static int sMAX_FRACTIONS = 10;

    /**
     * A big reduced fraction constant.
     */
    private static BigFraction sBigReducedFraction = 
        BigFraction.valueOf(new BigInteger("846122553600669882"),
                            new BigInteger("188027234133482196"),
                            true);

    /**
     * Stores a completed future with a BigFraction value of
     * sBigReducedFraction.
     */
    private static CompletableFuture<BigFraction> mBigReducedFractionFuture =
        CompletableFuture.completedFuture(sBigReducedFraction);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage "normal" (i.e., non-*Async())
        // methods.
        testFractionReduction();

        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage *Async() methods.
        testAsyncFractionReduction();

        // Test the use of a BigFraction constant using basic features
        // of a CompletableFuture and an explicit Java Thread.
        testFractionConstantThread();

        // Test BigFraction multiplication using basic features of
        // CompletableFuture and an explicit Java Thread.
        testFractionMultiplicationThread();

        // Test BigFraction multiplication using a CompletableFuture and
        // its runAsync() and join() methods.
        testFractionMultiplicationRunAsync();

        // Test BigFraction multiplication using a Callable, Future,
        // and the common fork-join pool.
        testFractionMultiplicationCallable();

        // Test BigFraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and join() method.
        testFractionMultiplicationSupplyAsync();

        // Test BigFraction multiplication using a CompletableFuture and
        // its completeAsync() factory method and join() method.
        testFractionMultiplicationCompleteAsync();

        // Test BigFraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and thenAccept()
        // completion stage method.
        testFractionMultiplicationAsyncChaining();

        // Test big fraction multiplication and addition using a
        // supplyAsync() and thenCombine().
        testFractionCombine();

        // Test BigFraction exception handling using
        // CompletableFutures and the handle() method.
        testFractionExceptions1();

        // Test BigFraction exception handling using
        // CompletableFutures and the exceptionally() method.
        testFractionExceptions2();

        // Test BigFraction exception handling using
        // CompletableFutures and the whenComplete() method.
        testFractionExceptions3();

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenCompose(), and acceptEither().
        testFractionMultiplications1();

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenComposeAsync(), and acceptEither().
        testFractionMultiplications2();

        // Block until user provides input and then exit to allow
        // future computations to complete running asynchronously.
        System.in.read();
    }

    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage "normal" (i.e., non-*Async())
     * methods.
     */
    private static void testFractionReduction() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReduction()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction =
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
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
            sb.append("     mixed reduced fraction = " + result);
            display(sb.toString());
        };

        CompletableFuture
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
    private static void testAsyncFractionReduction() {
        StringBuffer sb = 
            new StringBuffer(">> Calling testAsyncFractionReduction()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
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

        CompletableFuture
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
    private static void testFractionConstantThread() {
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
    }

    /**
     * Test BigFraction multiplication using basic features of a
     * CompletableFuture and an explicit Java Thread.
     */
    private static void testFractionMultiplicationThread() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationThread()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda multiplies
        // two large fractions.
        new Thread (() -> {
                BigFraction bf1 =
                    new BigFraction("62675744/15668936");
                BigFraction bf2 =
                    new BigFraction("609136/913704");
                    
                // Complete the future once the computation is
                // finished.
                future.complete(bf1.multiply(bf2));
        }).start();

        // Print the result, blocking until it's ready.
        sb.append("     Thread result = "
                  + future.join().toMixedString());
        display(sb.toString());
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * runAsync() and join() methods.
     */
    private static void testFractionMultiplicationRunAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationRunAsync()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // These "effectively final" objects are used to pass params
        // to the supplier lambda below.
        String f1 = "62675744/15668936";
        String f2 = "609136/913704";

        CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .runAsync(() -> {
                    BigFraction bf1 = new BigFraction(f1);
                    BigFraction bf2 = new BigFraction(f2);
                    
                    // Complete the future once the computation is
                    // finished.
                    future.complete(bf1.multiply(bf2));
                });

        // Print the result, blocking until it's ready.
        sb.append("     runAsync() result = "
                  + future.join().toMixedString());
        display(sb.toString());
    }

    /**
     * Test BigFraction multiplication using a Callable, Future, and
     * the common fork-join pool.
     */
    private static void testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        try {
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
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * supplyAsync() factory method and join() method.
     */
    private static void testFractionMultiplicationSupplyAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSupplyAsync()\n");

        // These "effectively final" objects are used to pass params
        // to the supplier lambda below.
        String f1 = "62675744/15668936";
        String f2 = "609136/913704";

        // Create a future that completes when the supplier submitted
        // to the common fork-join pool completes.
        CompletableFuture<BigFraction> future = CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .supplyAsync(() -> {
                    BigFraction bf1 = new BigFraction(f1);
                    BigFraction bf2 = new BigFraction(f2);
                    
                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });

        // Print the result, blocking until it's ready.
        sb.append("     supplyAsync() result = " 
                  + future.join().toMixedString());
        display(sb.toString());
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and
     * its completeAsync() factory method and join() method.
     */
    private static void testFractionMultiplicationCompleteAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCompleteAsync()\n");

        // These "effectively final" objects are used to pass params
        // to the supplier lambda below.
        String f1 = "62675744/15668936";
        String f2 = "609136/913704";

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Register an action that appends a string when run.
        future
                .thenRun(() -> sb.append("     completeAsync() result = "));

        // Complete this future with the result of multiplying two
        // large fractions together.
        future
                .completeAsync(() -> {
                    // Multiply two large fractions.
                    BigFraction bf1 = new BigFraction(f1);
                    BigFraction bf2 = new BigFraction(f2);

                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });

        // Append the result, blocking until it's ready.
        sb.append(future.join().toMixedString());

        // Print the result.
        display(sb.toString());
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * supplyAsync() factory method and thenAccept() completion stage
     * method.
     */
    private static void testFractionMultiplicationAsyncChaining() {
        StringBuffer sb = 
            new StringBuffer(">> Calling testFractionMultiplicationAsyncChaining()\n");

        // Create a supplier that multiplies two large fractions.
        Supplier<BigFraction> fractionMultiplier = () -> {
            BigFraction bf1 = new BigFraction("62675744/15668936");
            BigFraction bf2 = new BigFraction("609136/913704");
                    
            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Create a consumer that print the result as a mixed fraction
        // after it's reduced.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     Async chaining result = " 
                      + bigFraction.toMixedString());
            display(sb.toString());
        };

        CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .supplyAsync(fractionMultiplier)

            // This completion stage method is dispatched after the
            // BigFraction multiplication completes.
            .thenAccept(mixedFractionPrinter);
    }

    /**
     * Test big fraction multiplication and addition using a
     * supplyAsync() and thenCombine().
     */
    private static void testFractionCombine() {
        StringBuffer sb = 
            new StringBuffer(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf1 = CompletableFuture
            .supplyAsync(() ->
                         // This code runs asynchronously.
                         makeBigFraction(random, false)
                         .multiply(sBigReducedFraction));

        // Create another random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf2 = CompletableFuture
            .supplyAsync(() ->
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

        cf1
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
    private static void testFractionExceptions1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

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
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the exceptionally() method.
     */
    private static void testFractionExceptions2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions2()\n");

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
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the whenComplete() method.
     */
    private static void testFractionExceptions3() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions3()\n");

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
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenCompose(), and acceptEither().
     */
    private static void testFractionMultiplications1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications1()\n");

        // Lambda asynchronously reduces/multiplies a big fraction. 
        Function<BigFraction, CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(unreducedFraction))

            // thenCompose() is like flatMap(), i.e., it returns a
            // completable future to a multiplied big fraction.
            .thenCompose(reducedFraction -> CompletableFuture
                         // Multiply BigFractions asynchronously since
                         // it may run for a long time.
                         .supplyAsync(() 
                                      -> reducedFraction.multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:\n");

        // Process the two lambdas in a sequential stream.
        Stream
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
            .thenAccept(list -> sortAndPrintList(list,
                                                 sb));
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenComposeAsync(), and
     * acceptEither().
     */
    private static void testFractionMultiplications2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications2()\n");

        // Lambda asynchronously reduces/multiplies a big fraction. 
        Function<BigFraction, CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(unreducedFraction))

            // thenApplyAsync() returns a completable future to a big
            // fraction that's multiplied asynchronously since it may
            // run for a long time.
            .thenApplyAsync(reducedFraction
                            -> reducedFraction.multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:\n");

        // Process the two lambdas in a sequential stream.
        Stream
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
            .thenAccept(list -> sortAndPrintList(list,
                                                 sb));
    }

    /**
     * Sort the {@code list} in parallel using quicksort and mergesort
     * and then store the results in the {@code StringBuffer}
     * parameter.
     */
    private static void sortAndPrintList(List<BigFraction> list,
                                         StringBuffer sb) {
        // This implementation uses quick sort to order the list.
        CompletableFuture<List<BigFraction>> quickSortFuture = CompletableFuture
            // Perform quick sort asynchronously.
            .supplyAsync(() -> quickSort(list));

        // This implementation uses merge sort to order the list.
        CompletableFuture<List<BigFraction>> mergeSortFuture = CompletableFuture
            // Perform merge sort asynchronously.
            .supplyAsync(() -> mergeSort(list));

        // Select the result of whichever sort implementation
        // finishes first.
        quickSortFuture
            .acceptEither(mergeSortFuture, sortedList -> {
                    // Print the results as mixed fractions.
                    sortedList.forEach(fraction ->
                                       sb.append("     "
                                                 + fraction.toMixedString()
                                                 + "\n"));
                    display(sb.toString());
                });
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
    private static CompletableFuture<BigFraction> 
        makeBigFractionAsync(Random random,
                             boolean reduced) {
        return CompletableFuture
            .supplyAsync(() -> {
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
     * Perform a quick sort on the @a list.
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
     * Perform a merge sort on the @a list.
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
