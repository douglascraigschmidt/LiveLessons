package utils;

/**
 * Define a Java record that holds the "plain old data" (POD) for the
 * result of a primality check.
 */
public record PrimeResult(
    /*
     * Value that was evaluated for primality.
     */
    int primeCandidate,

    /*
     * Result of the isPrime() method.
     */
    int smallestFactor) {}

