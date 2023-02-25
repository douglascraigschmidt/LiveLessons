package edu.vandy.pubsub.common;

/**
 * A tuple containing the result of a prime check.
 */
public record Result(
    // Value that was evaluated for primality.
    int primeCandidate,

    // common.Result of the isPrime() method.
    int smallestFactor) {}
