import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java {@link List} {@code removeIf()} method.
 */
public class ex2 {
    // Entry point into the program.
    static public void main(String[] argv) {
        // A List containing odd and even numbers.
        List<Integer> list =
            // Create a mutable List.
            new ArrayList<>(List.of(1, 2, 3, 4, 5, 4, 3, 2, 1));

        // Print the items in the List.
        System.out.println(list);

        // Create a Predicate lambda that returns true if a number is
        // even, else false.
        Predicate<Integer> isEven = i -> i % 2 == 0;

        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(isEven);

        // Print the items in the stream.
        System.out.println(list);
    }
}

