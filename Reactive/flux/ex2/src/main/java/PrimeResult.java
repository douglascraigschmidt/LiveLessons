import java.math.BigInteger;

/**
 * Define a Java record that holds the data in a
 * result returned from checkIfPrime().
 */
public record PrimeResult(
    /*
     * Value that was evaluated for primality.
     */
    BigInteger primeCandidate,

    /*
     * Result of the isPrime() method.
     */
    BigInteger smallestFactor) {}

