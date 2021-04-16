import java.math.BigInteger;

/**
 * The result returned from checkIfPrime.
 */
public record PrimeResult(
    /*
     * Value that was evaluated for primality.
     */
    BigInteger mPrimeCandidate,

    /*
     * Result of the isPrime() method.
     */
    BigInteger mSmallestFactor) { }

