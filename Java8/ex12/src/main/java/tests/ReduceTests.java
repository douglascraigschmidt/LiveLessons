package tests;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static tests.Generators.sCharactersStr;

/**
 * These tests show how to use the modern Java reduce()
 * terminal operations.
 */
public class ReduceTests {
    /**
     * Run an example using the collect(groupingBy()) and the two-parameter
     * version of the reduce() terminal operations.
     */
    public static void runReduce1() {
        System.out.println("\nResults from runReduce1():");

        var results = Generators
            .generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name
                           // lengths.
                           + results);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = results
            // Extract values (i.e., Long count of string lengths)
            // from the map.
            .values()

            // Convert these values into a stream.
            .stream()

            // Trigger intermediate operations and sum up the length
            // of each name using a method reference.
            .reduce(0L,
                    // Instead could use (x, y) -> x + y.
                    Long::sum);

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example using the collect(groupingBy()) and the sum()
     * terminal operation.
     */
    public static void runReduce2() {
        System.out.println("\nResults from runReduce2():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = Generators
            .generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name
                           // lengths.
                           + results);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = results
            // Extract values (i.e., Long count of string lengths)
            // from the map.
            .values()

            // Convert these values into a stream.
            .stream()

            // Map Long values to primitive long values.
            .mapToLong(Long::longValue)

            // Trigger intermediate operations and sum the results.
            .sum();

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example using the collect(groupingBy()) and the
     * collect(summingInt()) terminal operation.
     */
    @SuppressWarnings("SimplifyStreamApiCallChains")
    public static void runReduce3() {
        System.out.println("\nResults from runReduce3():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = Generators
            .generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name
                           // lengths.
                           + results);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = results
            // Extract values (i.e., Long count of string lengths)
            // from the map.
            .values()

            // Convert these values into a stream.
            .stream()

            // Trigger the stream and sum the values into a single
            // long result.
            .collect(summingLong(Long::longValue));

            // Could also use
            // .mapToLong(Long::longValue)
            // .sum();

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example show the three-parameter reduce() terminal
     * operation, which also plays the role of "map" in map-reduce.
     */
    public static void runMapReduce1() {
        System.out.println("\nResults from runMapReduce1():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = Generators
            // Generate a Stream.
            .generateHCharacters(Pattern
                                 // Create a stream of characters from
                                 // William Shakespeare's Hamlet.
                                 .compile(",")
                                 .splitAsStream(sCharactersStr))

            // Terminal operation that triggers intermediate operation
            // processing and collects the results into a list.
            .toList();

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = results
            // Convert the list of strings into a stream of strings.
            .parallelStream()

            // Terminal operation that triggers intermediate operation
            // processing and uses the three-parameter version of
            // reduce() to sum the length of each name.  This approach
            // is overkill here, but is useful for more sophisticated
            // applications using parallel streams.
            .reduce(0L,
                    // This is the "map" operation
                    // (a.k.a.. "accumulator").
                    (sum, s) -> sum + s.length(),
                    // This is the "reduce" operation
                    // (a.k.a., "combiner").
                    Long::sum);

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * This example demonstrates the use of the three-parameter
     * reduce() operation, which is useful when the type being
     * streamed is different from the type of the accumulator.  In
     * particular, the inputs are Map.Entry (i.e., the 'entry'),
     * accumulator is a Double (i.e., the 'sum'), and the combiner
     * uses combines Double values.
     */
    public static void runMapReduce2() {
        System.out.println("\nResults from runMapReduce2():");

        // Immutable Map containing baseline prices of mobile devices.
        Map<String, Double> baseline = Map.ofEntries
            (Map.entry("iPhone12", 699.99),
             Map.entry("GalaxyS21", 799.99),
             Map.entry("Pixel6", 599.99),
             Map.entry("iPadPro", 999.99));

        // Immutable Map containing discount prices of mobile devices.
        Map<String, Double> actual = Map.ofEntries(
            Map.entry("iPhone12", 649.99),
            Map.entry("GalaxyS21", 769.99),
            Map.entry("Pixel6", 579.99)
        );

        Double percentageChange = baseline
            // Obtain the set of entries from the map.
            .entrySet()

            // Convert the map into a parallel stream.
            .parallelStream()

            // Use the three-parameter version of reduce() to compute
            // the percentage change of mobile device prices.
            .reduce(0.0,
                    // The accumulator operates on the Map's contents.
                    (Double sum, Map.Entry<String, Double> entry) -> {
                        // Track the percentage change for each device.
                        Double pc = 0.0;

                        // Compute the percentage change if there's a
                        // discount price for the mobile device.
                        if (actual.containsKey(entry.getKey())) {
                            Double baselinePrice = entry.getValue();
                            Double actualPrice = actual.get(entry.getKey());
                            pc = ((actualPrice - baselinePrice)
                                                / baselinePrice) * 100;
                        }

                        // Update the sum.
                        return sum + pc;
                    },

                    // The combiner sums Double values for parallel
                    // streams.
                    Double::sum);

        // Print the results.
        System.out.println("The total percentage change in prices ="
                           + percentageChange);
    }
}
