import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This example demonstrates various RxJava mechanisms for determining
 * if a flow of random big integers are prime numbers or not.  It
 * shows a sequential flow and two different concurrent flows.
 * It also illustrates the use of a memoizer based on Java's
 * ConcurrentHashMap.
 */
public class ex3 {
    /**
     * Maximum random number value.
     */
    private static int sMAX_VALUE = 1000000000;

    /**
     * A memoizer cache that maps candidate primes to their smallest
     * factor (if they aren't prime) or 0 if they are prime.
     */ 
    private static Map<BigInteger, BigInteger> mPrimeCache
        = new ConcurrentHashMap<>();

    /**
     * Max number of iterations.
     */
    private static int sMAX_ITERATIONS = 100;

    /**
     * Duration for sleeping.
     */
    private static long sSLEEP_DURATION = 100;

    /**
     * The scheduler to use for Observable.interval().
     */
    private static Scheduler sScheduler = Schedulers.computation();

    /**
     * The Java execution environment requires a static {@code main()}
     * entry point method to run the app.
     */
    static public void main(String[] argv) {
        // Test the sequential flow.
        testSequential();

        // Test the concurrent flow using an observable.
        testConcurrentObservable();

        // Test the concurrent flow using a flowable.
        testConcurrentFlowable();
    }

    /**
     * Test the sequential flow.
     */
    static private void testSequential() {
        System.out.println("begin testSequential()");

        // Use a single-threaded scheduler.
        sScheduler = Schedulers.trampoline();

        Observable
            // Factor method creates a flow of random big integers.
            .create(ex3::emit)

            // Use a memoizer to check if each random big integer
            // is prime or not on the main thread.
            .map(ex3::checkIfPrime)

            // Block and process each big integer on the main thread.
            .blockingSubscribe(ex3::processResult,
                               err -> print("ERROR" + err),
                               () -> print("DONE"));

        System.out.println("end testSequential()");
    }

    /**
     * Test the concurrent flow using an observable.
     */
    static private void testConcurrentObservable() {
        System.out.println("begin testConcurrentObservable()");

        Observable
            // Factor method creates a flow of random big integers.
            .create(ex3::emit)

            // Run all the operations in the computation thread pool.
            .subscribeOn(Schedulers.computation())

            // Use a memoizer to check if each random big integer is
            // prime or not.
            .map(ex3::checkIfPrime)

            // Block and process each big integer (runs in the calling thread).
            .blockingSubscribe(ex3::processResult,
                               err -> print("ERROR" + err),
                               () -> print("DONE"));

        System.out.println("end testConcurrentObservable()");
    }

    /**
     * Test the concurrent flow using a flowable.
     */
    static private void testConcurrentFlowable() {
        System.out.println("begin testConcurrentFlowable()");

        Flowable
            // Factor method creates a flow of random big integers.
            .create(ex3::emit, BackpressureStrategy.BUFFER)

            // Create a parallel flow of observables.
            .parallel()

            // Run checkIfPrime() in the computation thread pool.
            .runOn(Schedulers.computation())

            // Use a memoizer to check if each random big integer
            // is prime or not.
            .map(ex3::checkIfPrime)

            // Merge parallel flows back into a single flow.
            .sequential()

            // Block and process each big integer in the main thread.
            .blockingSubscribe(ex3::processResult,
                               err -> print("ERROR" + err),
                               () -> print("DONE"));

        System.out.println("end testConcurrentFlowable()");
    }

    /**
     * A factory method that bridges the reactive world with the
     * callback-style world to emit a flow of random big integers.
     *
     * @param emitter A callback object that omits a flow of random big integers
     */
    private static void emit(Emitter<BigInteger> emitter) {
        // Starting point of the random numbers.
        int origin = sMAX_VALUE - sMAX_ITERATIONS;

        // Random number generator.
        Random rand = new Random();

        Observable
            // Generate a flow of longs every sSLEEP_DURATION
            // milliseconds via the computation scheduler.
            .interval(sSLEEP_DURATION, TimeUnit.MILLISECONDS, sScheduler)

            // Generate random numbers between a range of min and max
            // values to ensure some duplicates.
            .map(x -> BigInteger.valueOf(rand.nextInt(sMAX_ITERATIONS) + origin))

            // Print the big integer as a debugging aid.
            .doOnNext(ex3::print)

            // Only take sMAX_ITERATIONS amount of big integers.
            .take(sMAX_ITERATIONS)

            // Block and emit each random number.
            .blockingSubscribe(emitter::onNext);

        // Shutdown the input stream.
        emitter.onComplete();
    }

    /**
     * This method checks whether the {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check for the prime factor.
     * @return a PrimeResult that contains the prime candidate and
     *         either 0 (if the prime candidate is prime) or the
     *         smallest factor (if it's not prime)
     */
    static PrimeResult checkIfPrime(BigInteger primeCandidate) {
        return new PrimeResult
            (primeCandidate,
             // This atomic "check then act" method serves as
             // a "memoizer" cache.
             mPrimeCache
             .computeIfAbsent(primeCandidate,
                              ex3::isPrime));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime or the smallest factor if it is not prime.
     */
    static BigInteger isPrime(BigInteger n) {
        print("checking if " + n + " is prime");

        BigInteger two = BigInteger.valueOf(2);

        if (n.mod(two).compareTo(BigInteger.ZERO) == 0) 
            return two;          

        for (BigInteger i = BigInteger.valueOf(3);
             n.compareTo(i.multiply(i)) >= 0;
             i = i.add(two)) 
            if (n.mod(i).compareTo(BigInteger.ZERO) == 0)
                return i;
    
        return BigInteger.ZERO;
    }

    /**
     * Process the {@code primeTuple} to print whether a number if prime.
     */
    private static void processResult(PrimeResult primeTuple) {
        if (!primeTuple.mSmallestFactor.equals(BigInteger.ZERO)) {
            print("found a non-prime number with smallest factor "
                  + primeTuple.mSmallestFactor
                  + " for "
                  + primeTuple.mPrimeCandidate);
        } else {
            print("found a prime number "
                  + primeTuple.mPrimeCandidate);
        }
    }

    /**
     * The result returned from checkIfPrime.
     */
    private static class PrimeResult {
        /**
         * Value that was evaluated for primality.
         */
        BigInteger mPrimeCandidate;

        /**
         * Result of the isPrime() method.
         */
        BigInteger mSmallestFactor;

        /**
         * Constructor initializes the fields.
         */
        public PrimeResult(BigInteger primeCandidate, BigInteger smallestFactor) {
            mPrimeCandidate = primeCandidate;
            mSmallestFactor = smallestFactor;
        }
    }

    /**
     * Print string {@code s} with the thread name appended.
     */
    private static void print(String s) {
        System.out.println(s 
                           + " in thread " 
                           + Thread.currentThread().getName());
    }

    /**
     * Print big integer {@code bigInteger} with the thread name appended.
     */
    private static void print(BigInteger bigInteger) {
        print("emitting " + bigInteger);
    }

    /**
     * Simple helper method that calls Thread.sleep() and catches InterruptedException.
     */
    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

