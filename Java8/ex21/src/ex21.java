import utils.RunTimer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This program shows the difference between stream sources (such as
 * List) that enforce encounter order and stream sources (such as
 * HashSet) that do not.  
 */
public class ex21 {
    /**
     * The maximum value in the range for the limitTest().
     */
    private final static int sLimitMax = 1000000000;

    /**
     * The maximum value in the range for the encounter order tests.
     */
    private final static int sEncounterMax = 10000000;

    /**
     * The number of output elements to print.
     */
    private final static int sOutputLimit = 10;

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        // Create a List of integers (where List enforces encounter
        // order).
        List<Integer> list =
            Arrays.asList(IntStream.range(0, sEncounterMax)
                          .boxed()
                          .toArray(Integer[]::new));

        // Create a HashSet of integers (where HashSet doesn't enforce
        // encounter order).
        Set<Integer> set =
            new HashSet<>(Arrays.asList(IntStream.range(0, sEncounterMax)
                                        .boxed()
                                        .toArray(Integer[]::new)));

        // Warm up the thread pool so the results are more accurate.
        warmUpThreadPool();

        // Run/time tests that show the difference in performance
        // between ordered and unordered uses of limit().
        runTestAndPrintResult(() -> limitTest(true),
                              "limitTest(unordered)");
        runTestAndPrintResult(() -> limitTest(false),
                              "limitTest(ordered)");

        // Run/time the tests that enforce encounter order, which will
        // take longer to run than those that don't.
        runTestAndPrintResult(() -> enforceEncounterOrder(false, list),
                              "enforceEncounterOrder(sequential)");

        runTestAndPrintResult(() -> enforceEncounterOrder(true, list),
                              "enforceEncounterOrder(parallel)");

        // Run/time the tests that do not enforce encounter order,
        // which will run faster than those that do.
        runTestAndPrintResult(() -> ignoreEncounterOrder(false, set),
                              "ignoreEncounterOrder(sequential)");
                    
        runTestAndPrintResult(() -> ignoreEncounterOrder(true, set),
                              "ignoreEncounterOrder(parallel)");

        // Print out the timing results for all the tests.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warm up the thread pool so the results are more accurate.
     */
    private static void warmUpThreadPool() {
        System.out.println("Warming up the thread pool");
        limitTest(false);
    }

    /**
     * Run the {@code supplier} and print the result for {@code testName}.
     */
    private static void runTestAndPrintResult(Supplier<Integer[]> supplier,
                                              String testName) {
        // Run/time the supplier and store the results into an array
        // of integers.
        Integer[] array =
                RunTimer.timeRun(supplier, testName);

        // Store the results in a string.
        String results = Arrays
            // Convert the array into a stream.
            .stream(array)

            // Only consider the last sOutputLimit elements.
            .skip(array.length - sOutputLimit)

            // Map each integer to a string.
            .map(Object::toString)

            // Collect into a single string separated by ' '.
            .collect(joining(" "));

        // Print the results.
        System.out.println("The last "
                           + sOutputLimit
                           + " even numbers = ["
                           + results
                           + "] "
                           + testName);

        // Let the system garbage collect.
        System.gc();
    }

    /**
     * Shows how encounter order is enforced if the source is ordered.
     * 
     * @param parallel Indicates whether or not the stream should run in parallel
     */
    private static Integer[] enforceEncounterOrder(boolean parallel,
                                                   Collection<Integer> list) {
        // Create a stream.
        Stream<Integer> intStream = list
            // Convert the list into a stream.
            .stream();

        // Make the stream parallel if directed.
        if (parallel)
            intStream.parallel();

        // Return an array of results that preserve the encounter order.
        return intStream
            // Only keep even numbers.
            .filter(x -> x % 2 == 0)

            // Double the integers in the list.
            .map(x -> x * 2)

            // Convert the stream into an integer array.
            .toArray(Integer[]::new);
    }

    /**
     * Shows how encounter order is ignored if the source is not
     * ordered.
     * 
     * @param parallel Indicates whether or not the stream should run in parallel
     */
    private static Integer[] ignoreEncounterOrder(boolean parallel,
                                                  Collection<Integer> set) {
        // Create a stream.
        Stream<Integer> intStream = set
            // Convert the list into a stream.
            .stream()

            // Ensure the stream is unordered.
            .unordered();

        // Make the stream parallel if directed.
        if (parallel)
            intStream.parallel();
            
        // Return an array of results that ignore encounter order.
        return intStream
            // Only keep even numbers.
            .filter(x -> x % 2 == 0)

            // Double the integers in the list.
            .map(x -> x * 2)

            // Convert the stream into an integer array.
            .toArray(Integer[]::new);
    }

    /**
     * Shows how the use of limit() is faster if the stream is
     * unordered vs ordered.
     * 
     * @param unordered Indicates whether the stream should be unordered or ordered
     */
    private static Integer[] limitTest(boolean unordered) {
        // Create a stream.
        IntStream intStream;

        // Make the stream unordered if directed.
        if (unordered)
            intStream = IntStream.range(0, sLimitMax)
                // Make the stream unordered.
                .unordered()

                // Create a parallel stream.
                .parallel();
        else
            // Convert the list into a parallel stream.
            intStream = IntStream.range(0, sLimitMax)
                .parallel();

        // Return an array of results that may or may not preserve
        // encounter order.
        return intStream
            // Only keep even numbers.
            .filter(x -> x % 2 == 0)
                           
            // Double the integers in the list.
            .map(x -> x * 2)

            // Limit the number of items in the output.
            .limit(sOutputLimit)

            // Box the results.
            .boxed()

            // Convert the stream into an integer array.
            .toArray(Integer[]::new);
    }
}
