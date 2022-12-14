package mathservices.common;

/**
 * Define a Java {@code record} that holds the "plain old data" (POD)
 * for the result of a primality check.
 */
public record PrimeResult(
    /*
     * Value evaluated for primality.
     */
    int primeCandidate,

    /*
     * Result of the {@code isPrime()} method.
     */
    int smallestFactor) {}

