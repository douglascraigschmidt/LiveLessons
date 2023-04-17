package zippyisms.utils;

import java.util.Random;

/** This Java utility class contains static methods
 *  that generate random {@link Integer} objects.
 */
public class RandomUtils {
    /**
     * Generate random {@link Integer} objects.
     *
     * @param count The number of random {@link Integer} objects
     * @param maxValue The maximum value of the random {@link Integer}
     *                 objects
     * @return An array of random {@link Integer} objects
     */
    public static Integer[] getRandomIntegers
        (int count,
         Integer maxValue) {
        return new Random()
            // Generate a stream containing 'count' random ints whose
            // values range from 0 to maxValue - 1.
            .ints(count,
                0,
                maxValue - 1)

            // Convert the IntStream of native ints into a Stream of
            // Integers.
            .boxed()

            // Trigger intermediate operations and store the random
            // Integer results in an array.
            .toArray(Integer[]::new);
    }
}
