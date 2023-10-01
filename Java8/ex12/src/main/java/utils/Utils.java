package utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Character.toLowerCase;

/**
 * This Java utility class contains static methods used by the ex12
 * driver program.
 */
final public class Utils {
    /**
     * A Java utility class should have a private constructor.
     */
    private Utils() { }

    /**
     * @return true if the {@link String} starts with 'H' or 'h'.     
     */
    public static Predicate<String> startsWithHh(boolean yes) {
        if (yes)
            return s -> toLowerCase(s.charAt(0)) == 'h';
        else
            return s -> toLowerCase(s.charAt(0)) != 'h';
    }

    /**
     * Capitalize {@code s} by making the first letter uppercase and
     * the rest lowercase.  This "pure" function's return value is
     * only determined by its input.
     */
    public static String capitalize(String s) {
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
     * @return The concatenation of {@link Collection} {@code c1} followed by
     * {@link Collection} {@code c2}
     */
    public static <T> Collection<T> concat(Collection<T> c1,
                                           Collection<T> c2) {
        // Append the contents of c2 at the end of c1.
        c1.addAll(c2);

        // Return the concatenated List.
        return c1;
    }
}
