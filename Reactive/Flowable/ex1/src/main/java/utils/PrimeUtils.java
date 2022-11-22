package utils;

import io.reactivex.rxjava3.functions.Function;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Java utility class that performs primality checks.
 */
public final class PrimeUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private PrimeUtils() {
    }
    /**
     * A record containing the result of a prime check.
     */
    public record Result (
        /*
         * Value that was evaluated for primality.
         */
        int primeCandidate,

        /*
         * Result of the isPrime() method.
         */
        int smallestFactor) {}

    /**
     * Check if {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check if it's prime
     */
    public static PrimeUtils.Result checkIfPrime(Integer primeCandidate)
        throws Throwable {
        // Return a record containing the prime candidate and the
        // result of checking if it's prime.
        return new Result(primeCandidate,
                          PrimeUtils.isPrime(primeCandidate));
    }

    /**
     * Determine whether {@code primeCandidate} is prime and
     * return the result.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if prime or the smallest factor if not prime
     */
    private static Integer isPrime(int primeCandidate) {
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
