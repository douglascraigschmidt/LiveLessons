import com.sun.istack.internal.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * This example shows a simple example of a Java 8 stream that
 * illustrates how it can be used with "pure" functions, i.e.,
 * functions whose return values are only determined by their input
 * values, without observable side effects.
 */
public class ex12 {
    static public void main(String[] argv) {
        new ex12().run();
    }

    /**
     * Run the example.
     */
    public void run() {
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
            .filter(s ->s.startsWith("h")||s.startsWith("H"))

            // Capitalize the first letter in the string.
            .map(this::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // Print out the results.
            .forEach(System.out::println);

    }
    /**
     * Capitalize @a s by making the first letter uppercase and the
     * rest lowercase.
     */
    private String capitalize(@NotNull String s) {
        if (s.length() == 0)
            return s;
        return s.substring(0, 1)
                .toUpperCase()
               + s.substring(1)
                  .toLowerCase();
    }
}

