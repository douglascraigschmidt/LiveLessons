package utils;

import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class defines static methods and fields used by
 * other classes.
 */
public class RandomUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private RandomUtils() {}

    /**
     * Generate and return a {@link List} of random {@link Integer}
     * objects of size 'count' that only contains odd numbers.
     *
     * @param count The number of random Integers to generate
     * @param maxValue The maximum value of the random {@link Integer}
     *        objects
     * @param oddOnly If true only generate odd numbers
     * @return A {@link List} of random {@link Integer} objects
     */
    public static List<Integer> generateRandomIntegers(int count,
                                                       int maxValue,
                                                       boolean oddOnly) {
        return new Random()
            // Generate "infinite" random ints.
            .ints(// Try to generate duplicates.
                  maxValue - count,
                  maxValue)

            // Only generate odd numbers if 'oddOnly' is true.
            .filter(n -> !oddOnly || n % 2 != 0)

            // Only take 'count' numbers.
            .limit(count)

            // Convert each primitive int to Integer.
            .boxed()

            // Trigger intermediate operations and collect into list.
            .toList();
    }
}
