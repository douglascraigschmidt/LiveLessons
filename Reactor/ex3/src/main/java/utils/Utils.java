package utils;

import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class defines static methods and fields used by
 * other classes.
 */
public class Utils {
    /**
     * A Java utility class should have a private constructor.
     */
    private Utils() {}

    /**
     * Generate and return a {@link List} of random {@link Integer}s.
     *
     * @param count The number of random Integers to generate
     * @param maxValue The maximum value of the random {@link Integer}s
     * @return A {@link List} of random {@link Integer}s
     */
    public static List<Integer> generateRandomIntegers(int count, int maxValue) {
        return new Random()
            // Generate "count" random ints.
            .ints(count,
                  // Try to generate duplicates.
                  maxValue - count,
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()

            // Trigger intermediate operations and collect into list.
            .collect(toList());
    }
}
