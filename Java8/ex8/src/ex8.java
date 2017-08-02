import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This example shows the use of the Java 8 completable future
 * framework to concurrently compute the greatest common divisor (GCD)
 * of two BigIntegers using several techniques.
 */
public class ex8 {
    /**
     * Number of big fractions to process asynchronously in a stream.
     */
    private static int sMAX_FRACTIONS = 10;

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        // Test fraction multiplication using basic features of
        // CompletableFuture and an explicit Java Thread.
        testFractionThread();

        // Test fraction multiplication using a CompletableFuture and
        // its runAsync() and join() methods.
        testFractionRunAsync();

        // Test fraction multiplication using a Callable and the
        // common fork-join pool.
        testFractionCallable();

        // Test fraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and join() method.
        testFractionSupplyAsync();

        // Test fraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and thenAccept()
        // completion stage method.
        testFractionAsyncChaining();

        // Test fraction multiplication using a CompletableFuture and
        // a chain of completion stage methods.
        testAsyncFractionReduction();

        // Test fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods.
        testAsyncFractionReductions();

        // Block until user provides input and then exit to allow
        // future computations to complete running asynchronously.
        System.in.read();
    }

    /**
     * Test fraction multiplication using basic features of a
     * CompletableFuture and an explicit Java Thread.
     */
    private static void testFractionThread() {
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
        System.out.println("Thread result = "
                           + future.join().toMixedString());
    }

    /**
     * Test fraction multiplication using a CompletableFuture and its
     * runAsync() and join() methods.
     */
    private static void testFractionRunAsync() {
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
        System.out.println("runAsync() result = "
                           + future.join().toMixedString());
    }

    /**
     * Test fraction multiplication using a Callable and the common
     * fork-join pool.
     */
    private static void testFractionCallable() {
        // These "effectively final" objects are used to pass params
        // to the callable lambda below.
        String f1 = "62675744/15668936";
        String f2 = "609136/913704";

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(f1);
            BigFraction bf2 = new BigFraction(f2);
                    
            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Submit the call to the common fork-join pool and store the
        // future it returns.
        Future<BigFraction> future =
                ForkJoinPool.commonPool().submit(call);

        BigFraction result = null;

        try {
            // Block until the result is available.
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Callable.call() = "
                           + result.toMixedString());
    }

    /**
     * Test fraction multiplication using a CompletableFuture and its
     * supplyAsync() factory method and join() method.
     */
    private static void testFractionSupplyAsync() {
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
        System.out.println("supplyAsync() result = " 
                           + future.join().toMixedString());
    }

    /**
     * Test fraction multiplication using a CompletableFuture and its
     * supplyAsync() factory method and thenAccept() completion stage
     * method.
     */
    private static void testFractionAsyncChaining() {
        // Create a supplier that multiplies two large fractions.
        Supplier<BigFraction> fractionMultiplier = () -> {
            BigFraction bf1 =
            new BigFraction("62675744/15668936");
            BigFraction bf2 =
            new BigFraction("609136/913704");
                    
            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Create a consumer that print the result as a mixed fraction
        // after it's reduced.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> System.out.println("Async chaining result = " 
                                  + bigFraction.toMixedString());

        CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .supplyAsync(fractionMultiplier)

            // This completion stage method is dispatched after the
            // BigFraction multiplication completes.
            .thenAccept(mixedFractionPrinter);
    }

    /**
     * Test fraction multiplication using a CompletableFuture and a
     * chain of completion stage methods.
     */
    private static void testAsyncFractionReduction() {
        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger ("846122553600669882"),
                                new BigInteger("188027234133482196"),
                                false);

        Supplier<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction =
                    BigFraction.reduce(unreducedFraction);

            System.out.println("unreducedFraction "
                               + unreducedFraction.toString()
                               + "\nreduced improper fraction = "
                               + reducedFraction.toString());

            // Return the reduction.
            return reducedFraction;
        };

        // Create a consumer to print the mixed reduced result.
        Consumer<String> printResult = result ->
                System.out.println("mixed reduced fraction = "
                        + result);

        CompletableFuture
            // Asynchronously reduce the unreduced big fraction.
            .supplyAsync(reduceFraction)

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .thenApply(BigFraction::toMixedString)

            // Print result after converting it to a mixed fraction.
            .thenAccept(printResult);
    }

    /**
     * Test fraction multiplication using a stream of
     * CompletableFutures and a chain of completion stage methods.
     */
    private static void testAsyncFractionReductions() {
        // Create a random number generator.
        Random random = new Random();

        // Create a supplier that generates big fractions.
        Supplier<BigFraction> generator = () -> {
            // Create a very large random big integer.
            BigInteger numerator =
                new BigInteger(150000, random);

            // Create a denominator that's between 1 to 10 times
            // smaller than the numerator.
            BigInteger denominator = 
                numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

            // Return an unreduced big fraction.
            return BigFraction.valueOf(numerator,
                                       denominator,
                                       false);
        };

        // Create a function to asynchronously reduce a big fraction.
        Function<BigFraction, CompletableFuture<BigFraction>> reduceFractions =
            unreducedFraction -> CompletableFuture
            .supplyAsync(() -> BigFraction.reduce(unreducedFraction));

        // Create a consumer that prints a list of reduced fractions.
        Consumer<List<BigFraction>> printList = list -> 
            list.forEach(reducedFraction ->
                         // Print fraction using "mixed" format.
                         System.out.println(reducedFraction.toMixedString()));

        // Create a future to a list of reduced big fractions.
        CompletableFuture<List<BigFraction>> futureToList = Stream
            // Generate sMAX_FRACTIONS random BigFraction objects.
            .generate(generator)
            .limit(sMAX_FRACTIONS)

            // Reduce these fractions asynchronously.
            .map(reduceFractions)

            // Trigger intermediate operation processing and return a
            // future to a list of reduced big fractions.
            .collect(FuturesCollector.toFutures());

        futureToList
            // After all the asynchronous fraction reductions have
            // completed print out the results.
            .thenAccept(printList);
    }
}
