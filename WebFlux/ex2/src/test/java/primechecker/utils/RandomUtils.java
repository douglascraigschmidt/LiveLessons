package primechecker.utils;

import reactor.core.publisher.Flux;

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
        // Initialize a new Random object for generating random numbers.
        Random random = new Random();

        // Define the minimum value to ensure the range is
        // (maxValue - count) to maxValue.
        int minValue = maxValue - count;

        // Create a Flux to generate an infinite stream of random integers,
        // process the stream, and collect the results into a List.
        return Flux
            // Generate an infinite stream of random Integers.
            .<Integer>generate(sink -> sink
                .next(minValue + random.nextInt(count)))

            // Filter the stream to include only odd numbers if oddOnly is true.
            .filter(n -> !oddOnly || (int) n % 2 != 0)

            // Limit the Flux to only 'count' number of elements.
            .take(count)

            // Collect the Flux stream into a List.
            .collectList()

            // Block until the processing is done and return the List.
            .block();
    }
}
