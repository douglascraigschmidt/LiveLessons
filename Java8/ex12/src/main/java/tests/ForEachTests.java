package tests;

import java.util.List;
import java.util.stream.Stream;

import static tests.Generators.sArray;
import static tests.Generators.sCharacters;

/**
 * These tests show how to use the modern Java forEach() and forEachOrdered()
 * terminal operations.
 */
public class ForEachTests {
    /**
     * Run an example using the forEach() terminal operation.
     */
    public static void runForEach1() {
        System.out.println("\nResults from runForEach1():");

        Generators
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
    public static void runForEach2() {
        System.out.println("\nResults from runForEach2():");

        Stream
            // Create a stream of Lists of characters from William
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
            .forEach(System.out::println);
    }

    /**
     * Run an example using the forEachOrdered() terminal operation
     * and show how to concatenate lists via Stream.of().
     */
    public static void runForEachOrdered() {
        System.out.println("\nResults from runForEachOrdered():");

        Stream<String> flattenedStream = Stream
            // Create a stream of lists of characters from William
            // Shakespeare's Hamlet using of() to concatenate lists.
            .of(sArray)

            // Process the stream in parallel, which is overkill for
            // this simple example.
            .parallel()

            // Convert the stream of lists of characters into a stream
            // of stream of characters.
            .map(List::stream)

            // Flatten the stream of stream of strings into a stream
            // of strings.
            .reduce(Stream::concat)
            .orElse(Stream.empty());

        Generators
            // Generate a Stream from flattenedStream.
            .generateHCharacters(flattenedStream)

            // This terminal operation triggers intermediate operation
            // processing and prints the results in "encounter order".
            .forEachOrdered(System.out::println);
    }
}
