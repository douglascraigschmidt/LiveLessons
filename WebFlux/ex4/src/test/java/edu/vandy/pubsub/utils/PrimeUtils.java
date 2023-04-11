package edu.vandy.pubsub.utils;

import edu.vandy.pubsub.common.Options;
import edu.vandy.pubsub.common.Result;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class defines static methods that perform
 * operations to check primality.
 */
public class PrimeUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private PrimeUtils() {}

    /**
     * Count # of calls to isPrime() to determine caching benefits.
     */
    public static AtomicInteger sPrimeCheckCounter =
        new AtomicInteger(0);

    /**
     * Check if {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check if it's prime
     * @param primeChecker A function that checks if number is prime
     * @return A {@code common.Result} object that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    public static Result checkIfPrime(Integer primeCandidate,
                                      Function<Integer, Integer> primeChecker) {
        // Return a tuple containing the prime candidate and the
        // result of checking if it's prime.
        return new Result(primeCandidate,
                          primeChecker.apply(primeCandidate));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    public static Integer isPrime(Integer primeCandidate) {
        // Increment the counter to indicate a prime candidate wasn't
        // already in the cache.
        sPrimeCheckCounter.incrementAndGet();

        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    // Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }


    /**
     * Print out the prime numbers in the {@code sortedMap}.
     */
    public static void printPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of prime integers.
        List<Integer> primes = Flux
                // Convert EntrySet of the map into a flux stream.
                .fromIterable(sortedMap.entrySet())

                // Slice the stream using a predicate that stops after a
                // non-prime # (i.e., getValue() != 0) is reached.
                .takeWhile(entry -> entry.getValue() == 0)

                // Map the EntrySet into just the key.
                .map(Map.Entry::getKey)

                // Collect the results into a list.
                .collect(toList())

                // Block until processing is done.
                .block();

        // Print out the list of primes.
        Options.print("primes =\n" + primes);
    }


    /**
     * Print out the non-prime numbers and their factors in the {@code
     * sortedMap}.
     */
    public static void printNonPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of non-prime integers and their factors.
        List<Map.Entry<Integer, Integer>> nonPrimes = Flux
                // Convert EntrySet of the map into a flux stream.
                .fromIterable(sortedMap.entrySet())

                // Slice the stream using a predicate that skips over the
                // non-prime #'s (i.e., getValue() == 0);
                .skipWhile(entry -> entry.getValue() == 0)

                // Collect the results into a list.
                .collect(toList())

                // Block until processing is done.
                .block();

        // Print out the list of primes.
        Options.print("non-prime numbers and their factors =\n"
                + nonPrimes);
    }
}

