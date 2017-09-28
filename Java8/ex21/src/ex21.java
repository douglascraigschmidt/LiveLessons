import utils.RunTimer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This program shows the difference between stream sources (such as
 * List) that enforce encounter order and stream sources (such as
 * HashSet) that do not in the context of various order-sensitive
 * aggregate operations, such as limit() and distinct().
 */
public class ex21 {
    /**
     * The maximum number of elements encounter order tests.
     */
    private final static int sEncounterMax = 100000000;

    /**
     * The number of output elements to print.
     */
    private final static int sOutputLimit = 10;

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        // Create a new random number generator.
        Random random = new Random();

        // Create a List of random integers between the values 100 and
        // 200 (List enforces encounter order).
        List<Integer> list =
            Arrays.asList(random.ints(sEncounterMax, 100, 200)
                          .boxed()
                          .toArray(Integer[]::new));

        // Create a HashSet of integers (HashSet doesn't enforce
        // encounter order).
        Set<Integer> set = new HashSet<>(list);

        // Warm up the thread pool so the results are more accurate.
        warmUpThreadPool(list);

        // Print first n items in the list in their "encounter order".
        printFirstNDistinctEvenNumbersDoubled(list,
                                              sOutputLimit,
                                              "list encounter order");

        // Run/time the tests that enforce encounter order, which will
        // take longer to run than those that ignore it.
        runTestAndPrintResult(() -> enforceEncounterOrder(false, list),
                              "enforceEncounterOrder(sequential)");

        runTestAndPrintResult(() -> enforceEncounterOrder(true, list),
                              "enforceEncounterOrder(parallel)");

        // Print first n items in the set in their "encounter order".
        printFirstNDistinctEvenNumbersDoubled(set,
                                              sOutputLimit,
                                              "set encounter order");

        // Run/time the tests that ignore encounter order, which will
        // run faster than those that enforce it.
        runTestAndPrintResult(() -> ignoreEncounterOrder(false, set),
                              "ignoreEncounterOrder(sequential)");
                    
        runTestAndPrintResult(() -> ignoreEncounterOrder(true, set),
                              "ignoreEncounterOrder(parallel)");

        // Run/time tests that show the difference in performance
        // between ordered and unordered uses of limit().
        runTestAndPrintResult(() -> limitTest(true, true, list),
                              "limitTest(unordered|parallel)");
        runTestAndPrintResult(() -> limitTest(false, true, list),
                              "limitTest(ordered|parallel)");
        runTestAndPrintResult(() -> limitTest(true, false, list),
                              "limitTest(unordered|sequential)");
        runTestAndPrintResult(() -> limitTest(false, false, list),
                              "limitTest(ordered|sequential)");

        // Run/time tests that show the difference in performance
        // between forEachOrdered() (which preserves order) and
        // forEach() (which does not preserve order).
        runTestAndPrintResult(() -> forEachTest(false, set),
                              "forEachTest(ordered)");
        runTestAndPrintResult(() -> forEachTest(true, set),
                              "forEachTest(unordered)");

        // Print out the timing results for all the tests.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warm up the thread pool so the results are more accurate.
     */
    private static void warmUpThreadPool(Collection<Integer> list) {
        System.out.println("Warming up the thread pool");
        limitTest(false, true, list);
    }

    /**
     * Print the first 
     */
    private static void printFirstNDistinctEvenNumbersDoubled(Collection<Integer> collection,
                                                              int n,
                                                              String testName) {
        // Store the results in a string.
        String results = collection
            // Convert the collection into a stream.
            .stream()

            // Ensure the results are distinct.
            .distinct()

            // Only keep even numbers.
            .filter(x -> x % 2 == 0)
                           
            // Double the integers in the list.
            .map(x -> x * 2)

            // Map each integer to a string.
            .map(Object::toString)

            // Limit the number of items in the output.
            .limit(n)

            // Collect into a single string separated by ' '.
            .collect(joining(" "));

        // Print the results.
        System.out.println("The first "
                           + n
                           + " even numbers = ["
                           + results
                           + "] "
                           + testName);

        // Let the system garbage collect.
        System.gc();
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

            // Map each integer to a string.
            .map(Object::toString)

            // Limit the number of items in the output.
            .limit(sOutputLimit)

            // Collect into a single string separated by ' '.
            .collect(joining(" "));

        // Print the results.
        System.out.println("The first "
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
            // Ensure the results are distinct.
            .distinct()

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
            // Ensure the results are distinct.
            .distinct()

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
     * @param parallel Indicates whether or not the stream should run in parallel
     */
    private static Integer[] limitTest(boolean unordered,
                                       boolean parallel,
                                       Collection<Integer> collection) {
        // A stream of integers.
        Stream<Integer> intStream;

        // Make the stream unordered if directed.
        if (unordered)
            // Create a stream.
            intStream = collection
                // Convert the collection into a stream.
                .stream()

                // Make the stream unordered.
                .unordered();
        else
            intStream = collection
                // Convert the collection into a stream.
                .stream();

        // Make the stream parallel if directed.
        if (parallel)
            intStream.parallel();

        // Return an array of results that may or may not preserve
        // encounter order.
        return intStream
            // Ensure the results are distinct.
            .distinct()

            // Only keep even numbers.
            .filter(x -> x % 2 == 0)
                           
            // Double the integers in the list.
            .map(x -> x * 2)

            // Limit the number of items in the output.
            .limit(sOutputLimit)

            // Convert the stream into an integer array.
            .toArray(Integer[]::new);
    }

    /**
     * Shows how the use of forEach() is faster than forEachOrdered() for
     * a parallel stream.
     * 
     * @param unordered Indicates whether the stream should be unordered or ordered
     */
    private static Integer[] forEachTest(boolean unordered,
                                         Collection<Integer> collection) {
        // Store the results in a concurrent queue.
        ConcurrentLinkedQueue<Integer> queue =
            new ConcurrentLinkedQueue<>();

        if (unordered)
            collection
                // Convert collection into a parallel stream.
                .parallelStream()

                // Ensure the results are distinct.
                .distinct()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)

                // Limit the number of items in the output.
                .limit(sOutputLimit)

                // Add each item to the list in an unordered manner.
                .forEach(queue::add);
        
        else 
            collection
                // Convert collection into a parallel stream.
                .parallelStream()

                // Ensure the results are distinct.
                .distinct()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)

                // Limit the number of items in the output.
                .limit(sOutputLimit)

                // Add each item to the list in an ordered manner.
                .forEachOrdered(queue::add);

        return queue.toArray(new Integer[0]);
    }
}
