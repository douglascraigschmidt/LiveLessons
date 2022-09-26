import utils.Options;
import utils.RunTimer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * This program shows the difference between Java streams sources
 * (such as {@link List}) that enforce encounter order and stream
 * sources (such as {@link HashSet}) that do not in the context of
 * various order-sensitive aggregate operations, such as {@code
 * limit()} and {@code distinct()}.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ex21 {
    /**
     * The maximum number of elements for the tests.
     */
    private static int sMaxIntegers;

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        // Process any command-line options.
        Options.instance().parseArgs(args);

        System.out.println("\nRunning the test program with "
                           + Options.instance().maxIntegers()
                           + " Integers");

        // Test characteristics of the distinct() operation.
        testDistinct();

        // Create a List of random integers, where List enforces
        // encounter order.
        List<Integer> list = generateRandomNumbers
            (Options.instance().maxIntegers());

        // Create a Set of integers, where Set doesn't enforce
        // encounter order.
        Set<Integer> set = new HashSet<>(list);

        // Warm up the thread pool so the results are more accurate.
        warmUpThreadPool(list);

        System.out.println("\nShowing encounter order vs. non-encounter order");

        // Print first n items in the list in their "encounter order".
        printFirstNDistinctEvenNumbersDoubled(list,
                                              Options.instance().outputLimit(),
                                              "list encounter order");

        // Print first n items in the set in their "non-encounter
        // order".
        printFirstNDistinctEvenNumbersDoubled(set,
                                              Options.instance().outputLimit(),
                                              "set non-encounter order");

                                              
        // Run the List tests.
        System.out.println("\n1. List tests");

        // Show how an ordered vs. unordered parallel stream performs
        // on a List.
        runTest(() -> listTest(false, list),
                "listTest(ordered)");
        runTest(() -> listTest(true, list),
                "listTest(unordered)");

        // Print out the timing results for the List tests.
        System.out.println(RunTimer.getTimingResults(true));

        // Run the forEach*() tests.
        System.out.println("2. forEach*() tests");

        // Run/time tests that show the difference in performance
        // between forEachOrdered() (which preserves order) and
        // forEach() (which does not preserve order).
        runTest(() -> forEachTest(false, list),
                "forEachOrdered() test");
        runTest(() -> forEachTest(true, list),
                "forEach() test");

        // Print out the timing results for the forEach*() tests.
        System.out.println(RunTimer.getTimingResults(true));

        // Run the encounter order vs. non-encounter order tests.
        System.out.println("3. Encounter order vs. non-encounter order tests");

        // Run/time the tests that enforce encounter order, which will
        // take longer to run than those that ignore it.
        runTest(() -> testEncounterOrder(false, false, list),
                "testEncounterOrder(enforce, sequential, list)");

        runTest(() -> testEncounterOrder(false, true, list),
                "testEncounterOrder(enforce, parallel, list)");

        // Run/time the tests that ignore encounter order, which will
        // run faster than those that enforce it.
        runTest(() -> testEncounterOrder(true, false, set),
                "testEncounterOrder(ignore, sequential, set)");
                    
        runTest(() -> testEncounterOrder(true, true, set),
                "testEncounterOrder(ignore, parallel, set)");

        // Print results for these tests.
        System.out.println(RunTimer.getTimingResults(true));

        // Run the limit tests.
        System.out.println("4. Ordered and unordered limit() tests");

        // Run/time tests that show the difference in performance
        // between ordered and unordered uses of limit() for
        // sequential and parallel streams.
        runTest(() -> limitTest(true, true, list),
                "limitTest(unordered|parallel)");
        runTest(() -> limitTest(false, true, list),
                "limitTest(ordered|parallel)");
        runTest(() -> limitTest(true, false, list),
                "limitTest(unordered|sequential)");
        runTest(() -> limitTest(false, false, list),
                "limitTest(ordered|sequential)");

        // Print out the timing results for the ordered and unordered
        // limit() tests.
        System.out.println(RunTimer.getTimingResults(true));
    }

    /**
     * This method demonstrates that the distinct() operation works
     * properly on unsorted streams.
     */
    private static void testDistinct() {
        // Create an unsorted List of integers containing duplicates.
        List<Integer> list = List.of(100, 30, 100, 40, 100, 10, 100);

        System.out.println("Distinct numbers = "
                         + list.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * Returns a {@link List} of randomly generated numbers.
     *
     * @return A {@link List} of randomly generated numbers
     */
    private static List<Integer> generateRandomNumbers(int maxIntegers) {
        return new Random()
            // Generate an unbounded stream of ints between
            // the values of 100 to 499.
            .ints(maxIntegers, 100, 500)

            // Convert ints to Integers.
            .boxed()

            // Collect into a List.
            .collect(toList());
    }

    /**
     * Print the first n distinct even numbers after doubling them.
     *
     * @param collection The {@link Collection} to process
     * @param n          The number of items to process
     * @param testName   The name of the test
     */
    private static void printFirstNDistinctEvenNumbersDoubled
        (Collection<Integer> collection,
         int n,
         String testName) {

        // Let the system garbage collect.
        System.gc();

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
        System.out.println(testName 
                           + ":\nthe first "
                           + n
                           + " even numbers = ["
                           + results
                           + "]");
    }

    /**
     * Run the {@link Supplier} and print the result for {@code
     * testName}.
     *
     * @param supplier The test to run 
     * @param testName The name of the test
     */
    private static void runTest(Supplier<Integer[]> supplier,
                                String testName) {
        // Let the system garbage collect.
        System.gc();

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
            .limit(Options.instance().outputLimit())

            // Collect into a single string separated by " ".
            .collect(joining(" "));

        if (Options.instance().diagnosticsEnabled())
            // Print the results.
            System.out.println(testName
                               + ":\nthe first "
                               + Options.instance().outputLimit()
                               + " even numbers = ["
                               + results
                               + "]");
    }

    /**
     * Shows how the use of {@code forEach()} on an unordered parallel
     * stream can be faster than {@code forEachOrdered()} on an
     * ordered parallel stream.
     * 
     * @param unordered  Indicates whether the stream should be
     *                   processed in an unordered or ordered manner
     * @param list       The {@link List} to process 
     * @return A array containing the results
     */
    private static Integer[] forEachTest(boolean unordered,
                                         List<Integer> list) {
        if (unordered) {
            // Store the results in a concurrent queue since forEach()
            // isn't synchronized.
            ConcurrentLinkedQueue<Integer> queue =
                new ConcurrentLinkedQueue<>();

            list
                // Convert collection into a parallel stream.
                .parallelStream()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)

                // Add each item to the list in an unordered manner,
                // which assumes a thread-safe queue.
                .forEach(queue::add);

            return queue.toArray(new Integer[0]);
        }
        else {
            // Store the results in an unsynchronized ArrayList since
            // forEachOrdered *is* synchronized.
            List<Integer> queue =
                new ArrayList<>();

            list
                // Convert collection into a parallel stream.
                .parallelStream()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)

                // Add each item to the list in an ordered manner.
                .forEachOrdered(queue::add);

            return queue.toArray(new Integer[0]);
        }
    }

    /**
     * Show how an ordered vs. unordered parallel stream performs on a
     * {@link List}.
     * 
     * @param unordered  Indicates whether the stream should be
     *                   processed in an unordered or ordered manner
     * @param list       The {@link List} to process 
     * @return A {@link Integer} array containing the results
     */
    private static Integer[] listTest(boolean unordered,
                                      List<Integer> list) {
        if (unordered) {
            return list
                // Convert collection into a parallel stream.
                .parallelStream()

                // Unorder the results.
                .unordered()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)

                // Create an array.
                .toArray(Integer[]::new);
        }
        else {
            return list
                // Convert collection into a parallel stream.
                .parallelStream()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)
                
                // Create an array.
                .toArray(Integer[]::new);
        }
    }

    /**
     * Shows how encounter order behaves if the source is ordered
     * vs. unordered and whether the stream is parallel or sequential.
     * 
     * @param unordered Indicates whether the stream should be
     *                  unordered or ordered
     * @param parallel  Indicates whether or not the stream should run
     *                  in parallel
     * @param collection The {@link Collection} to process
     * @return A array containing the results
     */
    private static Integer[] testEncounterOrder(boolean unordered,
                                                boolean parallel,
                                                Collection<Integer> collection) {
        // Convert the collection into a stream.
        Stream<Integer> intStream = collection.stream();

        // Make the stream parallel if directed.
        if (parallel)
            intStream.parallel();

        // Make the stream unordered if directed.
        if (unordered)
            // Return an array of doubled even numbers.
            return intStream
                // Make the stream unordered.
                .unordered()

                // Ensure the results are distinct.
                // .distinct()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)

                // Double the integers in the list.
                .map(x -> x * 2)

                // Convert the stream into an integer array.
                .toArray(Integer[]::new);
        else
            // Return an array of doubled even numbers.
            return intStream

                // Ensure the results are distinct.
                // .distinct()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)

                // Double the integers in the list.
                .map(x -> x * 2)

                // Convert the stream into an integer array.
                .toArray(Integer[]::new);
    }

    /**
     * Shows how the use of {@code limit()} is faster if the stream is
     * unordered vs ordered.
     * 
     * @param unordered Indicates whether the stream should be
     *                  unordered or ordered
     * @param parallel Indicates whether or not the stream should run
     *                 in parallel
     * @param list     The {@link List} to process 
     * @return A array containing the results
     */
    private static Integer[] limitTest(boolean unordered,
                                       boolean parallel,
                                       List<Integer> list) {
        // A stream of integers.
        Stream<Integer> intStream = list.stream();

        // Make the stream parallel if directed.
        if (parallel)
            intStream.parallel();

        // Make the stream unordered if directed.
        if (unordered)
            return intStream
                // Make the stream unordered.
                .unordered()

                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)
                
                // Limit the number of items in the output.
                .limit(Options.instance().maxIntegers() / 1_000)

                // Convert the stream into an integer array.
                .toArray(Integer[]::new);
        else
            return intStream
                // Only keep even numbers.
                .filter(x -> x % 2 == 0)
                           
                // Double the integers in the list.
                .map(x -> x * 2)
                
                // Limit the number of items in the output.
                .limit(Options.instance().maxIntegers() / 1_000)

                // Convert the stream into an integer array.
                .toArray(Integer[]::new);
    }

    /**
     * Warm up the thread pool so the results are more accurate.
     *
     * @param list The {@link List} to use for the warmup
     */
    private static void warmUpThreadPool(List<Integer> list) {
        System.out.println("Warming up the thread pool");
        limitTest(false, true, list);
    }
}
