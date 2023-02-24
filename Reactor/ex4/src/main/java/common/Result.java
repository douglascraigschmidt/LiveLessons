package common;

/**
 * A tuple containing the result of a prime check.
 */
public class Result {
    /**
     * Value that was evaluated for primality.
     */
    int mPrimeCandidate;

    /**
     * common.Result of the isPrime() method.
     */
    int mSmallestFactor;

    /**
     * Constructor initializes the fields.
     */
    public Result(int primeCandidate, int smallestFactor) {
        mPrimeCandidate = primeCandidate;
        mSmallestFactor = smallestFactor;
    }
}
