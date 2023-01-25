package utils;

import java.util.List;
import java.util.Random;

/**
 * This Java utility class provides a method that creates a {@link
 * List} of random {@link String} objects.
 */
public final class RandomUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private RandomUtils() {}

    /**
     * @return {@code numberOfInts} random {@link Integer} objects
     */
    public static List<String> getRandomInts(long numberOfInts,
                                             int lowerBound,
                                             int upperBound) {
        return new Random()
            // Create a Stream of random ints.
            .ints(numberOfInts,
                  lowerBound,
                  upperBound)

            // Convert the ints to String objects.
            .mapToObj(String::valueOf)

            // Convert the Stream to a List.
            .toList();
    }
}
