package utils;

import java.math.BigInteger;

/**
 * Define a Java record that holds the "plain old data" (POD) in a
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

