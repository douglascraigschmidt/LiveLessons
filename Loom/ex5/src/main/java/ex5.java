import jdk.incubator.concurrent.StructuredTaskScope;
import utils.ShutdownOnNonNullSuccess;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static utils.PrimeUtils.isPrime;

/**
 * This example demonstrates how to use {@link StructuredTaskScope.ShutdownOnSuccess}, as
 * well as a custom {@link StructuredTaskScope}, to capture the result
 * of the first subtask to complete successfully (i.e., identify a
 * prime number without returning a {@code null}).  You'll need to
 * install JDK 19 (or beyond) with gradle version 7.6 (or beyond)
 * configured to run this example.
 */
public class ex5 {
    /**
     * Create a {@link List} of large numbers, both prime and
     * non-prime.
     */
    private static final List<Long> sNumbers = List
        .of(3968514575694708773L,
            5667979166914025678L,
            5667979166914025677L,
            5667979166914025676L,
            5180857017854625097L,
            5180857017854625096L,
            7231103070891537647L,
            7231103070891537644L,
            6381999300585923437L,
            6381999300585923438L);

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        System.out.println("Entering test");

        // Demonstrate how apply a Java ShutdownOnSuccess including
        // odd numbers.
        demoShutdownOnSuccess(sNumbers);

        // Demonstrate how apply a custom Java StructuredTaskScope
        // including odd numbers.
        demoCustomStructuredTaskScope(sNumbers);

        // Demonstrate how apply a Java ShutdownOnSuccess excluding
        // odd numbers.
        demoShutdownOnSuccess(removeOddNumbers(sNumbers));

        // Demonstrate how apply a custom Java StructuredTaskScope
        // excluding odd numbers.
        demoCustomStructuredTaskScope(removeOddNumbers(sNumbers));

        System.out.println("Leaving test");
    }

    /**
     * Demonstrate how to use {@link StructuredTaskScope.ShutdownOnSuccess}
     * used to capture the result of the first subtask to complete
     * successfully, i.e., identify a prime number in {@code numbers}.
     */
    public static void demoShutdownOnSuccess(List<Long> numbers) {
        // Create a new scope to execute virtual threads.
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Long>()) {
            numbers
                // Concurrently check whether the numbers are prime or
                // not.
                .forEach(number -> scope
                         // Create a virtual thread.
                         .fork(() -> {
                                 // If number is prime yield it,
                                 // otherwise throw an exception.
                                 if (isPrime(number) == 0)
                                     return number;
                                 else
                                     throw new NoSuchElementException();
                             }));

            // Perform a barrier synchronization that waits for the
            // first successful computation to find a prime number or
            // all of them to fail.
            var result = scope.join().result();

            // Print the first prime number (or null).
            System.out.println("First prime result = " + result);
            // Don't exit the try-with-resources scope until all
            // concurrently executing virtual threads complete.
        } catch (Exception exception) {
            System.out.println("No prime results found "
                               + exception.getMessage());
        }
    }

    /**
     * Demonstrate how to create a custom {@link StructuredTaskScope}
     * that's used to capture the result of the first subtask to
     * complete successfully, i.e., identify a prime number in {@code
     * numbers} without returning a {@code null}).
     */
    public static void demoCustomStructuredTaskScope(List<Long> numbers) {
        // Create a new scope to execute virtual threads.
        try (var scope = new ShutdownOnNonNullSuccess<Long>()) {
            numbers
                // Concurrently check whether the numbers are prime or
                // not.
                .forEach(number -> scope
                         // Create a virtual thread.
                         .fork(() ->
                                 // If number is prime yield it,
                                 // otherwise yield null.
                                isPrime(number) == 0
                                ? number : null));

            // Perform a barrier synchronization that waits for the
            // first successful computation to find a prime number or
            // all of them to fail.
            var result = scope.join().result();

            if (result != null)
                // Print the first prime number (or null).
                System.out.println("First prime result = " + result);
            else
                System.out.println("No prime results found");

            // Don't exit the try-with-resources scope until all
            // concurrently executing virtual threads complete.
        } catch (Exception exception) {
            System.out.println("Exception: " 
                               + exception.getMessage());
        }
    }

    /**
     * Remove odd numbers from {@code numbers}.
     */
    private static List<Long> removeOddNumbers(List<Long> numbers) {
        return numbers
            // Convert List to Stream.
            .stream()

            // Keep only even numbers.
            .filter(l -> l % 2 == 0)

            // Convert Stream to List.
            .toList();
    }
}
