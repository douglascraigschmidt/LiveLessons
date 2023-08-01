package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Utility class for generating random data for use by the
 * various {@link Map} implementations.
 */
public class RandomUtils {
    /**
     * Generate random data for use by the various {@link Map}
     * implementations.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    public static List<Integer> generateRandomData
        (int count, int maxValue) {
        // This List will hold random large integers.
        List<Integer> randomIntegers = new ArrayList<>();
        Random random = new Random();

        // Generate "count" random integers between (maxValue - count)
        // and maxValue.
        for (int i = 0; i < count; i++) {
            // Generate a random number between (maxValue - count) and
            // maxValue to ensure duplicates.
            int randomNumber = random
                .nextInt(maxValue - (maxValue - count))
                        + (maxValue - count);

            // Add the random number to the List of random integers.
            randomIntegers.add(randomNumber);
        }

        // Return the List of random integers.
        return randomIntegers;
    }
}
