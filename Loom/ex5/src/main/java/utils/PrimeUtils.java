package utils;

/**
 * This Java utility class contains a static method that performs
 * primality checks.
 */
public final class PrimeUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private PrimeUtils() {}

    /**
     * This method checks if number {@code primeCandidate} is prime.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if {@code primeCandidate} is prime, or the smallest
     *         factor if it is not prime
     */
    public static long isPrime(long primeCandidate) {
        // Check if primeCandidate is a multiple of 2.
        if (primeCandidate % 2 == 0)
            // Return smallest factor for non-prime number.
            return 2;

        // If not, then just check the odds for primality.
        for (long factor = 3;
             factor * factor <= primeCandidate;
             // Skip over even numbers.
             factor += 2)
            if (primeCandidate % factor == 0)
                // primeCandidate was not prime.
                return factor;

        // primeCandidate was prime.
        return 0;
    }
}
