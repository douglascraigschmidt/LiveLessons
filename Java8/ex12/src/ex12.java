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
 * also shows how Java Streams can be used with "pure" functions,
 * i.e., functions whose return values are only determined by their
 * input values, without any side effects.
 */
public class ex12  {
    static public void main(String[] argv) {
        // Create an instance of this class.
        ex12 ex = new ex12();

        // Demonstrate various approaches.
        ex.runClassicJava();
        ex.runForEach();
        ex.runFlatMapLimit();
        ex.runForEachOfConcatenation();
        ex.runCollectToList();
        ex.runCollectJoining();
        ex.runCollectToSet();
        ex.runCollectToMap();
        ex.runCollectGroupingBy();
        ex.runCollectReduce1();
        ex.runCollectReduce2();
        ex.runCollectReduce3();
        ex.runCollectMapReduce();
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

        List<String> listOfCharacters = new LinkedList<>
            (List.of("horatio",
                     "claudius",
                     "Gertrude",
                     "Hamlet",
                     "laertes",
                     "Ophelia"));

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

        Stream
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "laertes",
                "Ophelia")

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

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

        // Create several lists containing characters from Hamlet.
        List<String> l1 = List.of("Hamlet",
                                  "claudius",
                                  "Gertrude");
        List<String> l2 = List.of("Ophelia",
                                  "laertes",
                                  "Polonius");
        List<String> l3 = List.of("Reynaldo",
                                  "horatio",
                                  "Voltemand",
                                  "Cornelius",
                                  "Rosencrantz",
                                  "Gildenstern");
        List<String> l4 = List.of("Fortinbras");

        Stream
            // Create a stream of characters from William
            // Shakespeare's Hamlet using of() to concatenate lists.
            .of(l1, l2, l3, l4)

            // Flatten the stream of lists of strings into a stream of
            // strings.
            .flatMap(strings -> {
                // Print strings to see how far we go in the stream!
                System.out.println (strings);
                return strings.stream();
            })

            // Limit the output to the first 4 elements in the stream.
            .limit(4)

            // This terminal operation triggers aggregate operation
            // processing and prints the results in "encounter order".
            .forEachOrdered(System.out::println);
    }

    /**
     * Run an example using the forEachOrdered() terminal operation
     * and show how to concatenate lists via Stream.of().
     */
    private void runForEachOfConcatenation() {
        System.out.println("\nResults from runForEachOfConcatenation():");

        // Create several lists containing characters from Hamlet.
        List<String> l1 = List.of("Hamlet",
                                  "claudius",
                                  "Gertrude");
        List<String> l2 = List.of("Ophelia",
                                  "laertes",
                                  "Polonius");
        List<String> l3 = List.of("Reynaldo",
                                  "horatio",
                                  "Voltemand",
                                  "Cornelius",
                                  "Rosencrantz",
                                  "Gildenstern");
        List<String> l4 = Collections.singletonList("Fortinbras");

        Stream
            // Create a stream of characters from William
            // Shakespeare's Hamlet using of() to concatenate lists.
            .of(l1, l2, l3, l4)

            // Process the stream in parallel, which is overkill for
            // this simple example.
            .parallel()

            // Flatten the stream of lists of strings into a stream of
            // strings.
            .flatMap(List::stream)

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // This terminal operation triggers aggregate operation
            // processing and prints the results in "encounter order".
            .forEachOrdered(System.out::println);
    }

    /**
     * Run an example using the collect() terminal operation to put
     * the results into a ArrayList using the toList() collector.
     */
    private void runCollectToList() {
        System.out.println("\nResults from runCollectToList():");

        // Create a list of key characters in Hamlet.
        List<String> characters = List
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "Hamlet", // Hamlet appears twice.
                "laertes",
                "Ophelia");

        // Create sorted list of characters starting with 'h' or 'H'.
        List<String> results = characters
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // This terminal operation triggers aggregate operation
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

        // Create a list of key characters in Hamlet.
        List<String> characters = List
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "Hamlet", // Hamlet appears twice.
                "laertes",
                "Ophelia");

        // Create String containing the sorted list of characters
        // starting with 'h' or 'H'.
        String results = characters
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // This terminal operation triggers aggregate operation
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

        // Create a list of key characters in Hamlet.
        List<String> characters = List
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "Hamlet", // Hamlet appears twice.
                "laertes",
                "Ophelia");

        // Create sorted set of characters starting with 'h' or 'H'.
        Set<String> results = characters
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // This terminal operationd that triggers aggregate
            // operation processing and collects the results into a
            // set (which contains no duplicates).
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

        // Create a list of key characters in Hamlet.
        List<String> characters = List
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "Hamlet", // Hamlet appears twice.
                "laertes",
                "Ophelia");

        // Create sorted set of characters starting with 'h' or 'H'.
        Map<String, Integer> results = characters
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Terminal operation that triggers aggregate operation
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

        // Create a list of key characters in Hamlet.
        List<String> characters = List
            .of("horatio",
                "claudius",
                "Gertrude",
                "Hamlet",
                "Hamlet", // Hamlet appears twice.
                "laertes",
                "Ophelia");

        // Create sorted set of characters starting with 'h' or 'H'.
        Map<String, Long> results = characters
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Terminal operation that triggers aggregate operation
            // processing and collects the results into a map.
            // Terminal operation that triggers aggregate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));

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

        // Create map of Hamlet characters starting with 'h' or 'H'
        // (key) and the length of each characters name (value).
        Map<String, Long> matchingCharactersMap = Pattern
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .compile(",")
            .splitAsStream("horatio,claudius,Gertrude,Hamlet,laertes,Ophelia")

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Terminal operation that triggers aggregate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
                           + matchingCharactersMap);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = matchingCharactersMap
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
                           + matchingCharactersMap.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example using the collect(groupingBy()) and the sum()
     * terminal operation.
     */
    private void runCollectReduce2() {
        System.out.println("\nResults from runCollectReduce2():");

        // Create map of Hamlet characters starting with 'h' or 'H'
        // (key) and the length of each characters name (value).
        Map<String, Long> matchingCharactersMap = Pattern
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .compile(",")
            .splitAsStream("horatio,claudius,Gertrude,Hamlet,laertes,Ophelia")

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Terminal operation that triggers aggregate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
                           + matchingCharactersMap);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = matchingCharactersMap
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
                           + matchingCharactersMap.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example using the collect(groupingBy()) and the
     * collect(summingInt()) terminal operation.
     */
    private void runCollectReduce3() {
        System.out.println("\nResults from runCollectReduce3():");

        // Create map of Hamlet characters starting with 'h' or 'H'
        // (key) and the length of each characters name (value).
        Map<String, Long> matchingCharactersMap = Pattern
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .compile(",")
            .splitAsStream("horatio,claudius,Gertrude,Hamlet,laertes,Ophelia")

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Terminal operation that triggers aggregate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));

        // Print the results.
        System.out.println("Hamlet characters' names + name lengths "
                           // Get the list of character names and name lengths.
                           + matchingCharactersMap);

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = matchingCharactersMap
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
                           + matchingCharactersMap.keySet()
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }

    /**
     * Run an example show the three parameter reduce() terminal
     * operation, which also plays the role of "map" in map-reduce.
     */
    private void runCollectMapReduce() {
        System.out.println("\nResults from runCollectMapReduce():");

        List<String> characterList = Pattern
            // Create a stream of characters from William
            // Shakespeare's Hamlet.
            .compile(",")
            .splitAsStream("horatio,claudius,Gertrude,Hamlet,laertes,Ophelia")

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> toLowerCase(s.charAt(0)) == 'h')

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // Terminal operation that triggers aggregate operation
            // processing and collects the results into a list.
            .collect(toList());

        // Count of the length of each Hamlet character names that
        // start with 'h' or 'H'.
        long countOfCharacterNameLengths = characterList
            // Convert the list of strings into a stream of strings.
            .parallelStream()

            // Terminal operation that triggers aggregate operation
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
                           + characterList
                           + " starting with 'h' or 'H' = "
                           + countOfCharacterNameLengths);
    }
}

