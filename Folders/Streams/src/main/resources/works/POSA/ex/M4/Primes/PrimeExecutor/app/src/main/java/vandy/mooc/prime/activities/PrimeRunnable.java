package vandy.mooc.prime.activities;

/**
 * Uses a brute-force algorithm to determine if a given number is
 * prime or not.
 */
public class PrimeRunnable
        implements Runnable {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG =
            getClass().getSimpleName();

    /**
     * A reference to the MainActivity.
     */
    private final MainActivity mMainActivity;

    /**
     * Number to evaluate for "primality".
     */
    private final long mPrimeCandidate;

    /**
     * Constructor initializes the fields.
     */
    public PrimeRunnable(MainActivity mainActivity,
                         long primeCandidate) {
        mMainActivity = mainActivity;
        mPrimeCandidate = primeCandidate;
    }

    /**
     * This method provides a brute-force determination of whether
     * number @a n is prime.  Returns 0 if it is prime, -1 if operation
     * has been cancelled, or the smallest factor if it is not prime.
     */
    private long isPrime(long n) {
        if (n > 3)
            for (long factor = 2; factor <= n / 2; ++factor) {
                if (n / factor * factor == n) {
                    return factor;
                }
            }

        return 0;
    }

    /**
     * Hook method that determines if a given number is prime.
     */
    public void run() {
        // Determine if mPrimeCandidate is prime or not.
        long smallestFactor = isPrime(mPrimeCandidate);

        // Allow activity to update UI stats to reflect this result.
        mMainActivity.updateResults(mPrimeCandidate, smallestFactor);

        // Tell the activity we're done.
        mMainActivity.done();
    }
}
