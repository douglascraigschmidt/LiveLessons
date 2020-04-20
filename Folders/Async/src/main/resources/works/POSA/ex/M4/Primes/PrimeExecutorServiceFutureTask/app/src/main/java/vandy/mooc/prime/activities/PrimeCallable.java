package vandy.mooc.prime.activities;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Uses a supplied function to determine if a given number is prime or
 * not.
 */
public class PrimeCallable
       implements Callable<PrimeCallable.PrimeResult> {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG =
        getClass().getSimpleName();

    /** 
     * Number to evaluate for "primality".
     */
    private final long mPrimeCandidate;

    /**
     * This function checks if a number if prime.
     */
    private Function<Long, Long> mPrimeChecker;

    /**
     * The result returned via the future.
     */
    public static class PrimeResult {
        /**
         * Value that was evaluated for primality.
         */
        final long mPrimeCandidate;
        
        /**
         * Result of the isPrime() method.
         */
        final long mSmallestFactor;
        
        /**
         * Constructor initializes the fields.
         */
        PrimeResult(long primeCandidate, long smallestFactor) {
            mPrimeCandidate = primeCandidate;
            mSmallestFactor = smallestFactor;
        }
    }

    /**
     * Constructor initializes the fields.
     */
    PrimeCallable(long primeCandidate,
                  Function<Long, Long> primeChecker) {
        mPrimeCandidate = primeCandidate;
        mPrimeChecker = primeChecker;
    }
    
    /**
     * Hook method that determines if a given number is prime.
     * Returns 0 if it is prime or the smallest factor if it is not
     * prime.
     */
    public PrimeResult call() {
        // Return a PrimeResult containing the prime candidate and the
        // result of checking this number for primality.
        return new PrimeResult(mPrimeCandidate,
                               // Determine if mPrimeCandidate is
                               // prime or not.
                               mPrimeChecker.apply(mPrimeCandidate));
    }
}
