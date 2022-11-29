package utils;

import java.util.List;
import java.util.Random;

/**
 * This Java utility class contains static methods that generate random numbers.
 */
public final class RandomUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private RandomUtils() {}

    /**
     * Generate a {@link List} of random {@link Integer} objects used
     * to check primality.
     */
    public static List<Integer> generateRandomNumbers(int count, int maxValue) {
        // Generate a List of random Integer objects.
        return new Random()
            // Generate the given # of large random ints.
            .ints(count,
                  maxValue - count,
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into a List.
            .toList();
    }
}
