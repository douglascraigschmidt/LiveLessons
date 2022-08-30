import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java {@link List} {@code removeIf()} method.
 */
public class ex2 {
    // Entry point into the program.
    static public void main(String[] argv) {
        // A List containing odd and even numbers.
        List<Integer> list =
            // Must wrap the immutable List with an ArrayList.
            new ArrayList<>(List.of(1, 2, 3, 4, 5, 4, 3, 2, 1));

        // Print the count of the items in the stream.
        System.out.println(list);

        // Create a Predicate that returns true if a number is
        // even, else false.
        Predicate<Integer> isEven = i -> i % 2 == 0;

        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(isEven);

        // Print the count of the items in the stream.
        System.out.println(list);
    }
}

