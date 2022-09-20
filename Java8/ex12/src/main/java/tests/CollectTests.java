package tests;

import utils.Utils;

import java.util.stream.Collector;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static tests.Generators.sCharacters;
import static tests.Generators.sCharactersStr;
import static utils.Utils.startsWithHh;

/**
 * These tests show how to use the modern Java collect()
 * terminal operation with various pre-defined {@link Collector}
 * implementations.
 */
public class CollectTests {
    /**
     * Run an example using the collect() terminal operation to put
     * the results into a ArrayList using the toList() collector.
     */
    public static void runCollectToList() {
        System.out.println("\nResults from runCollectToList():");

        var results = Generators
            // Generate a Stream.
            .generateHCharacters(sCharacters.stream())

            // This terminal operation triggers intermediate operation
            // processing and collects the results into a list, which
            // contains duplicates.
            .collect(toList());

        // Print the results.
        System.out.println(results);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a HashSet using the toSet() collector.
     */
    public static void runCollectToSet() {
        System.out.println("\nResults from runCollectToSet():");

        var results = Generators
            // Generate a Stream.
            .generateHCharacters(sCharacters.stream())

            // This terminal operation triggers intermediate operation
            // processing and collects the results into a set (which
            // contains no duplicates).
            .collect(toSet());

        // Print the results.
        System.out.println(results);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a HashMap using the toMap() collector.
     */
    public static void runCollectToMap() {
        System.out.println("\nResults from runCollectToMap():");

        var results = Generators
            // Generate a Stream.
            .generateHCharacters(sCharacters.stream())

            // Terminal operation that triggers intermediate operation
            // processing and collects the results into a map.
            .collect(toMap(identity(), String::length, Integer::sum));

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names.
                           + results);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a TreeMap using the groupingBy() collector.
     */
    public static void runCollectGroupingBy() {
        System.out.println("\nResults from runCollectGroupingBy():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = Generators.
            generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names.
                           + results);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a String using the joining() collector.
     */
    public static void runCollectJoining() {
        System.out.println("\nResults from runCollectJoining():");

        var results = Generators
                // Generate a Stream.
                .generateHCharacters(sCharacters.stream())

                // This terminal operation triggers intermediate operation
                // processing and collects the results into a String,
                // which contains duplicates.
                .collect(joining(" "));

        // Print the results.
        System.out.println(results);
    }

    /**
     * Run an example using the collect() terminal operation in
     * conjunction with the {@code teeing} {@link Collector}.
     */
    public static void runTeeingCollector() {
        System.out.println("\nResults from runTeeingCollector():");

        var results = sCharacters
            // Generate a Stream.
            .stream()

            // Capitalize the first letter in the string.
            .map(Utils::capitalize)

            // Sort the elements in ascending order.
            .sorted()

            // Trigger the intermediate operations and collect the
            // results via the teeing Collector. 
            .collect(
                     // Collect all  characters starting with 'H' or
                     // 'h' into one List followed by collecting all
                     // characters not starting with 'H' or 'h' into
                     // a separate List.
                     teeing(// Filter out non 'H' or 'h' characters.
                            filtering(startsWithHh(true),
                                      toList()),
                            // Filter out 'H' or 'h' characters.
                            filtering(startsWithHh(false),
                                      toList()),
                            // Merge the Lists so the 'H'/'h'
                            // characters comes first followed
                            // by the non-'H'/'h' characters.
                            Utils::concat));

        // Print the results.
        System.out.println(results);
    }

}
