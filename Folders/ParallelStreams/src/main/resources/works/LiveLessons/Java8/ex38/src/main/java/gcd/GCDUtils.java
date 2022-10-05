package gcd;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

/**
 * This Java utility class defines methods that compute GCD results.
 */
public final class GCDUtils {
    /**
     * The random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Create a {@link List} of random {@link GCDParam} objects.
     */
    static final List<GCDParam> sRANDOM_PARAMS = IntStream
        // Iterate from 1 to 1_000_000.
        .rangeClosed(1, 1_000_000)

        // Create a stream of GCDParam objects initialized to random
        // values.
        .mapToObj(___ -> 
                  new GCDParam(abs(sRANDOM.nextInt()), 
                               abs(sRANDOM.nextInt())))

        // Trigger intermediate operations and collect into list.
        .toList();

    /**
     * A Java utility class should have a private constructor.
     */
    private GCDUtils() {}

    /**
     * Generate random data for use by the various hashmaps.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    public static List<GCDParam> getRandomData(int count) {
        return sRANDOM_PARAMS
            // Convert the List into a Stream.
            .stream()

            // Limit the size of the stream by 'count'.
            .limit(count)

            // Collect the results into a List.
            .toList();
    }

    /**
     * Compute the GCD of the two-element array {@code integers}.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD
     * @return A {@link GCDResult}
     */
    public static GCDResult computeGCD(GCDParam integers) {
        // Create a record to hold the GCD results.
        return new GCDResult(integers,
                             gcd(integers));
    }

    /**
     * Provides an iterative implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD) of {@code number1}
     * and {@code number2}.
     */
    private static int gcd(GCDParam integers) {
        int number1 = integers.first();
        int number2 = integers.second();
        for (;;) {
            int remainder = number1 % number2;
            if (remainder == 0)
                return number2;
            else{
                number1 = number2;
                number2 = remainder;
            }
        }
    }
}
