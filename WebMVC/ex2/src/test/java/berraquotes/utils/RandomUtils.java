package berraquotes.utils;

import java.util.List;
import java.util.Random;

/**
 * This Java utility class contains static methods that generate
 * random numbers.
 */
public final class RandomUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private RandomUtils() {}

    /**
     * Generate a {@link List} of random {@link Integer} objects.
     */
    public static List<Integer> makeRandomIndices(int numberOfIndices,
                                                  int numberOfQuotes) {
        return new Random()
            // Generate a stream containing 'numberOfIndices' random
            // ints whose values range from 1 and the total number of
            // quotes.
            .ints(numberOfIndices,
                  1,
                  numberOfQuotes)

            // Convert the IntStream of native ints into a Stream of
            // Integers.
            .boxed()

            // Trigger intermediate operations and store the random
            // Integer results in an array.
            .toList();
    }
}
