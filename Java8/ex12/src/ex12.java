import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Character.toLowerCase;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * This program shows many modern Java Streams terminal operations,
 * including forEach*(), collect(), and several variants of reduce().
 * In addition, it includes a classic Java example as a baseline.  It
 * also shows how Java Streams can be used with "pure" functions
 * (i.e., functions whose return values are only determined by their
 * input values) that have no side effects.
 */
public class ex12 {
    /** 
     * A {@link List} of key characters in the play Hamlet.
     */
    private final List<String> sCharacters = List
        .of("horatio",
            "claudius",
            "Gertrude",
            "Fortinbras", // Fortinbras appears twice.
            "fortinbras",
            "Hamlet",
            "Hamlet", // Hamlet appears twice.
            "laertes",
            "Ophelia");

    /**
     * A {@link String} of key characters in the play Hamlet.
     */
    private final String sCharactersStr =
        "horatio,claudius,Gertrude,Fortinbras,fortinbras,Hamlet,Hamlet,laertes,Ophelia";

    /**
     * Create an array of {@link List} objects containing characters
     * from Hamlet.
     */
    List<String>[] sArray = new List[]{
        List.of("Hamlet",
                "claudius",
                "Gertrude"),
        List.of("Ophelia",
                "laertes",
                "Polonius"),
        List.of("Reynaldo",
                "horatio",
                "Voltemand",
                "Cornelius",
                "Rosencrantz",
                "Gildenstern"),
        List.of("Fortinbras")};

    /**
     * The main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance of this class.
        ex12 ex = new ex12();

        // Demonstrate the Java 7 baseline.
        ex.runClassicJava();

        // Demonstrate the forEach() terminal operations.
        forEachTests(ex);

        // Demonstrate the collect() terminal operations.
        collectTests(ex);

        // Demonstrate the reduce() terminal operations.
        reduceTests(ex);
    }

    /**
     * Demonstrate the forEach() terminal operations.
     */
    private static void forEachTests(ex12 ex) {
        ex.runForEach();
        ex.runFlatMapLimit();
        ex.runForEachOfConcatenation();
    }

    /**
     * Demonstrate the collect() terminal operations.
     */
    private static void collectTests(ex12 ex) {
        ex.runCollectToList();
        ex.runCollectJoining();
        ex.runCollectToSet();
        ex.runCollectToMap();
        ex.runCollectGroupingBy();
    }
    
    /**
     * Demonstrate the reduce() terminal operations.
     */
    private static void reduceTests(ex12 ex) {
        ex.runCollectReduce1();
        ex.runCollectReduce2();
        ex.runCollectReduce3();
        ex.runCollectMapReduce();
        ex.runCollectMapReduceEx();
    }

    /**
     * Capitalize {@code s} by making the first letter uppercase and
     * the rest lowercase.  This "pure" function's return value is
     * only determined by its input.
     */
    private String capitalize(String s) {
        if (s.length() == 0)
            return s;
        return s
            // Uppercase the first character of the string.
            .substring(0, 1)
            .toUpperCase()
            // Lowercase the remainder of the string.
            + s.substring(1)
            .toLowerCase();
    }

    /**
     * Run an example using only classic Java features, which serves
     * as a baseline for comparing with modern Java solutions.
     */
    private void runClassicJava() {
        System.out.println("Results from runClassicJava():");

        List<String> listOfCharacters = new ArrayList<>(sCharacters);

        // Loop through all the characters.
        for (int i = 0; i < listOfCharacters.size();) {
            // Remove any strings that don't start with 'h' or 'H'.
            if (toLowerCase(listOfCharacters.get(i).charAt(0)) != 'h') {
                listOfCharacters.remove(i);
            } else {
                // Capitalize the first letter of a character whose
                // names starts with 'H' or 'h'.
                listOfCharacters.set(i, capitalize(listOfCharacters.get(i)));
                i++;
            }
        }

        // Sort the results in ascending order.
        Collections.sort(listOfCharacters);

        // Print the results.
        for (String s : listOfCharacters)
            System.out.println(s);
    }

    /**
     * Run an example using the forEach() terminal operation.
     */
    private void runForEach() {
        System.out.println("\nResults from runForEach():");

        this
            // Generate a Stream.
            .generateHCharacters(sCharacters.stream())

            // Terminal operation that triggers intermediate operation
            // processing and prints the results.
            .forEach(System.out::println);
    }

    /**
     * Run an example using the flatMap() intermediate operation that
     * illustrates how flatMap() is a stateless operation.
     */
    private void runFlatMapLimit() {
        System.out.println("\nResults from runFlatMapLimit():");

        Stream
            // Create a stream of characters from William
            // Shakespeare's Hamlet using of() to concatenate lists.
            .of(sArray)

            // Flatten the stream of lists of strings into a stream of
            // strings.
            .flatMap(strings -> {
                    // Print strings to see how far we go in the stream!
                    System.out.println (strings);
                    return strings.stream();
                })

            // Limit the output to the first 4 elements in the stream.
            .limit(4)

            // This terminal operation triggers intermediate operation
            // processing and prints the results in "encounter order".
            .forEachOrdered(System.out::println);
    }

    /**
     * Run an example using the forEachOrdered() terminal operation
     * and show how to concatenate lists via Stream.of().
     */
    private void runForEachOfConcatenation() {
        System.out.println("\nResults from runForEachOfConcatenation():");

        Stream<String> flattenedStream = Stream
            // Create a stream of characters from William
            // Shakespeare's Hamlet using of() to concatenate lists.
            .of(sArray)

            // Process the stream in parallel, which is overkill for
            // this simple example.
            .parallel()

            // Flatten the stream of lists of strings into a stream of
            // strings.
            .flatMap(List::stream);

        this
            .generateHCharacters(flattenedStream)

            // This terminal operation triggers intermediate operation
            // processing and prints the results in "encounter order".
            .forEachOrdered(System.out::println);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a ArrayList using the toList() collector.
     */
    private void runCollectToList() {
        System.out.println("\nResults from runCollectToList():");

        var results = this
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
     * the results into a String using the joining() collector.
     */
    private void runCollectJoining() {
        System.out.println("\nResults from runCollectJoining():");

        var results = this
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
     * Run an example using the collect() terminal operation to put
     * the results into a HashSet using the toSet() collector.
     */
    private void runCollectToSet() {
        System.out.println("\nResults from runCollectToSet():");

        var results = this
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
    private void runCollectToMap() {
        System.out.println("\nResults from runCollectToMap():");

        var results = this
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
    private void runCollectGroupingBy() {
        System.out.println("\nResults from runCollectGroupingBy():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names.
                           + results);
    }

    /**
     * Run an example using the collect(groupingBy()) and the two
     * parameter version of the reduce() terminal operations.
     */
    private void runCollectReduce1() {
        System.out.println("\nResults from runCollectReduce1():");

        var results = generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
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
    private void runCollectReduce2() {
        System.out.println("\nResults from runCollectReduce2():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
                           + results);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = results
            // Extract values (i.e., Long count of string lengths)
            // from the map.
            .values()

            // Convert these values into a stream.
            .stream()

            // Map values to long.
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
    private void runCollectReduce3() {
        System.out.println("\nResults from runCollectReduce3():");

        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = generateHCharactersMap(sCharactersStr);

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
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

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example show the three parameter reduce() terminal
     * operation, which also plays the role of "map" in map-reduce.
     */
    private void runCollectMapReduce() {
        System.out.println("\nResults from runCollectMapReduce():");


        // Generate a Map of characters from the play Hamlet whose
        // keys start with upper- or lower-case 'h' whose names are
        // consistently capitalized and whose values are the lengths
        // of the character's names.
        var results = this
            // Generate a Stream.
            .generateHCharacters(Pattern
                                 // Create a stream of characters from William
                                 // Shakespeare's Hamlet.
                                 .compile(",")
                                 .splitAsStream(sCharactersStr))

            // Terminal operation that triggers intermediate operation
            // processing and collects the results into a list.
            .collect(toList());

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
                    // This is the "map" operation.
                    (sum, s) -> sum + s.length(),
                    // This is the "reduce" operation.
                    Long::sum);

        // Print the results.
        System.out.println("Count of lengths of Hamlet characters' names "
                           // Get the list of character names.
                           + results
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * @return A sorted {@link Stream} of characters from the play
     * Hamlet whose names start with upper- or lower-case 'h' whose
     * names are consistently capitalized.
     */
    private Stream<String> generateHCharacters(Stream<String> characters) {
        return characters
            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted();
    }

    /**
     * @return A {@link Map} of characters from the play Hamlet whose
     * keys start with upper- or lower-case 'h' whose names are
     * consistently capitalized and whose values are the lengths of
     * the character's names.
     */
    private Map<String, Long> generateHCharactersMap(String characters) {
        return this
            // Generate a Stream.
            .generateHCharacters(Pattern
                                 // Create a stream of characters from William
                                 // Shakespeare's Hamlet.
                                 .compile(",")
                                 .splitAsStream(characters))

            // Terminal operation that triggers intermediate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));
    }

    /**
     * This example demonstrates the use of the three-parameter
     * reduce() operation, which is useful when the type being
     * streamed is different from the type of the accumulator.  In
     * particular, the inputs are Map.Entry (i.e., the 'entry'),
     * accumulator is a Double (i.e., the 'sum'), and the combiner
     * uses combines Double values.
     */
    private void runCollectMapReduceEx() {
        System.out.println("\nResults from runCollectMapReduceEx():");

        Map<String, Double> baseline = new HashMap<>() {
                { put("AZ", 0.00123); }
                { put("MI", 0.02497); }
                { put("WI", 0.01238); }
                { put("WY", 0.04232); }
            };

        Map<String, Double> actual = new HashMap<>() {
                { put("AZ", 0.01023); }
                { put("MI", 0.09497); }
                { put("WI", 0.00238); }
            };

        Double breakoutFactor = baseline
            // Obtain the set of entries from the map.
            .entrySet()

            // Convert the map into a stream.
            .stream()

            // Use the three-parameter version of reduce().
            .reduce(0.0,
                    // The accumulator operates on the Map's contents.
                    (Double sum, Map.Entry<String, Double> entry) -> {
                        Double difference = entry.getValue();
                        if (actual.containsKey(entry.getKey())) 
                            difference =
                                Math.abs(entry.getValue() - actual.get(entry.getKey()));

                        return sum + difference;
                    },

                    // The combiner just sums Double values.
                    Double::sum);

        // Print the results.
        System.out.println("The breakout factor = " 
                           + breakoutFactor);
                    
    }
}

