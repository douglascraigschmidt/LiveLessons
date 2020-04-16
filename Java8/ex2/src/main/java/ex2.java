import java.util.*;
import java.util.stream.Stream;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java List/ArrayList removeIf() method.
 */
public class ex2 {
    static public void main(String[] argv) {
        List<Integer> list =
            new ArrayList<Integer>(List.of(1, 2, 3, 4, 5));

        // Print the count of the items in the stream.
        countIt(list.stream());
        System.out.println(list);
        
        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(i -> i % 2 == 0);

        // Print the count of the items in the stream.
        countIt(list.stream());
        System.out.println(list);
    }

    /**
     * Print the count of items in the {@code stream}.
     */
    private static void countIt(Stream<Integer> stream) {
        System.out.println("count = " + stream.count());
    }
}

