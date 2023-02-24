package edu.vandy.pubsub.common;

/**
 * A tuple containing the result of a prime check.
 */

public class Result {
        // Value that was evaluated for primality.
        public int primeCandidate;

        // common.Result of the isPrime() method.
        public int smallestFactor;

        public Result(int primeCandidate, int smallestFactor) {
            this.primeCandidate = primeCandidate;
            this.smallestFactor = smallestFactor;
        }
}

/*
public record Result(
    // Value that was evaluated for primality.
    int primeCandidate,

    // common.Result of the isPrime() method.
    int smallestFactor) {}

 */
