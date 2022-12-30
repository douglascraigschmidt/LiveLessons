package edu.vandy.mathservices.utils;

/**
 * This Java utility class contains static methods that perform
 * primality and GCD functions.
 */
public final class MathUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private MathUtils() {}

    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD) of {@code number1}
     * and {@code number2}.
     */
    public static int gcd(int number1, int number2) {
        // Basis case.
        if (number2 == 0)
            return number1;
        // Recursive call.
        return gcd(number2,
                   number1 % number2);
    }

    /**
     * This method checks if number {@code primeCandidate} is prime.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if {@code primeCandidate} is prime, or the smallest
     *         factor if it is not prime
     */
    public static int isPrime(int primeCandidate) {
        // Check if primeCandidate is a multiple of 2.
        if (primeCandidate % 2 == 0)
            // Return smallest factor for non-prime number.
            return 2;

        // If not, then just check the odds for primality.
        for (int factor = 3;
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
