package utils;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static utils.BigFractionUtils.makeBigFraction;

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

    /**
     * @return A {@link List} of {@code count}random and unreduced
     *         {@link BigFraction} objects
     */
    public static List<BigFraction> generateRandomBigFractions(int count) {
        return Stream
            // Generate an infinite stream of random and unreduced BigFraction
            // objects.
            .generate(() ->
                      makeBigFraction(new Random(), false))

            // Limit the size of the stream to 'count' items.
            .limit(count)

            // Trigger intermediate processing and collect the results
            // into a List.
            .toList();
    }
}
