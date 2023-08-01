package utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class contains utility methods for prime number checking.
 */
public class PrimeUtils {
    /**
     * Define a Java record that holds the "plain old data" (POD) for
     * the result of a primality check.
     */
    public record PrimeResult(/*
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
     * @param primeCandidate The number to check for primality
     * @param memoizer A cache that avoids rechecking if a number is prime
     * @return A {@link PrimeResult} record that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    public static PrimeResult checkIfPrime
        (Integer primeCandidate,
         Function<Integer, Integer> memoizer) {
        // Return a record containing the prime candidate and the
        // result of checking if it's prime.
        return new PrimeResult(primeCandidate,
                               memoizer.apply(primeCandidate));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if it is prime or the smallest factor if it is not
     *         prime
     */
    public static Integer isPrime(Integer primeCandidate) {
        // Increment the counter to indicate a prime candidate wasn't
        // already in the cache.
        Options.instance().primeCheckCounter().incrementAndGet();

        int n = primeCandidate;

        if (n > 3)
            // This "brute force" algorithm is intentionally
            // inefficient to burn lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * Print out the prime numbers in {@code sortedMap}.
     */
    public static void printPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of prime integers.
        List<Integer> primes = sortedMap
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Slice the stream using a predicate that stops after a
            // non-prime number (i.e., getValue() != 0) is reached.
            .takeWhile(entry -> entry.getValue() == 0)

            // Map the EntrySet into just the key.
            .map(Map.Entry::getKey)

            // Collect the results into a list.
            .toList();

        // Print out the list of primes.
        Options.print("primes =\n" + primes);
    }

    /**
     * Print out the non-prime numbers and their factors in {@code
     * sortedMap}.
     */
    public static void printNonPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of non-prime integers and their factors.
        List<Map.Entry<Integer, Integer>> nonPrimes = sortedMap
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Slice the stream using a predicate that skips over the
            // non-prime numbers (i.e., getValue() == 0);
            .dropWhile(entry -> entry.getValue() == 0)

            // Collect the results into a list.
            .toList();

        // Print out the list of primes.
        Options.print("non-prime numbers and their factors =\n"
                      + nonPrimes);
    }
}

