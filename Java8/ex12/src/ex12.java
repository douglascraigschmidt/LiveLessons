import java.util.*;

/**
 * This example shows a simple example of a Java 8 stream that
 * illustrates how it can be used with "pure" functions, i.e.,
 * functions whose return values are only determined by their input
 * values, without observable side effects.
 */
public class ex12 {
    static public void main(String[] argv) {
        // Create a list of characters from William Shakespeare's Hamlet.
        List<String> list = 
            Arrays.asList("horatio",
                          "claudius",
                          "Gertrude",
                          "Hamlet",
                          "laertes",
                          "Ophelia");

        list
            // Convert the list into a stream.
            .stream()

            // Remove any strings that don't start with 'h' or 'H'.
            .filter(s -> s.startsWith("h") || s.startsWith("H"))

            // Capitalize the first letter in the string.
            .map(ex12::capitalize)

            // Sort the results in ascending order.
            .sorted()

            // Print out the results.
            .forEach(System.out::println);
    }

    /**
     * Capitalize @a s by making the first letter uppercase and the rest lowercase.
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0)
            return s;
        return s.substring(0, 1)
                .toUpperCase()
                + s.substring(1)
                   .toLowerCase();
    }
}

