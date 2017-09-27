import utils.RunTimer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * This program shows the difference between stream sources (such as
 * List) that enforce encounter order and stream sources (such as
 * HashSet) that do not.
 */
public class ex21 {
    /**
     * The maximum value in the range.
     */
    private final static int sMAX = 1000;

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        // Run/time the tests that enforce encounter order, which will
        // take longer to run than those that don't.
        RunTimer.timeRun(() -> enforceEncounterOrder(false),
                         "enforceEncounterOrder(sequential)");
        RunTimer.timeRun(() -> enforceEncounterOrder(true),
                         "enforceEncounterOrder(parallel)");

        // Run/time the tests that do not enforce encounter order,
        // which will run faster than those that do.
        RunTimer.timeRun(() -> ignoreEncounterOrder(false),
                         "ignoreEncounterOrder(sequential)");
        RunTimer.timeRun(() -> ignoreEncounterOrder(true),
                         "ignoreEncounterOrder(parallel)");

        // Print out the timing results for all the tests.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Shows how encounter order is enforced if the source is ordered.
     * 
     * @param parallel Indicates whether or not the stream should run in parallel
     */
    private static void enforceEncounterOrder(boolean parallel) {
        // Create a List of integers (where List enforces encounter
        // order).
        List<Integer> list =
            Arrays.asList(IntStream.range(0, sMAX)
                          .boxed()
                          .toArray(Integer[]::new));

        // Create a stream.
        Stream<Integer> integerStream = list
            // Convert the list into a stream.
            .stream();

            // Make the stream parallel if directed.
            if (parallel)
                integerStream.parallel();

        // List of results.
        List<Integer> doubledList = integerStream
            // Only keep numbers that are multiples of 100.
            .filter(x -> x % 100 == 0)

                // Double the integers in the list.
            .map(x -> x * 2)

            // Collect the results into a list
            .collect(toList());

        // Print the results.
        System.out.println(doubledList);
    }

    /**
     * Shows how encounter order is ignored if the source is not
     * ordered.
     * 
     * @param parallel Indicates whether or not the stream should run in parallel
     */
    private static void ignoreEncounterOrder(boolean parallel) {
        // Create a HashSet of integers (where HashSet doesn't enforce
        // encounter order).
        Set<Integer> set =
            new HashSet(Arrays.asList(IntStream.range(0, sMAX)
                                      .boxed()
                                      .toArray(Integer[]::new)));

        // Create a stream.
        Stream<Integer> integerStream = set
            // Convert the list into a stream.
            .stream();

            // Make the stream parallel if directed.
            if (parallel)
                integerStream.parallel();

        // List of results.
        Set<Integer> doubledSet = integerStream
            // Only keep numbers that are multiples of 100.
            .filter(x -> x % 100 == 0)

            // Double the integers in the list.
            .map(x -> x * 2)

            // Collect the results into a set
            .collect(toSet());

        // Print the results.
        System.out.println(doubledSet);
    }
}
