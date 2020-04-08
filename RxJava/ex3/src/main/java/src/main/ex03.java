package src.main.java;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import src.main.java.utils.ExceptionUtils;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This example ...
 */
public class ex03 {
    /**
     * Maximum random number value.
     */
    private static int MAX_VALUE = 1000000000;

    /**
     * Cache that maps candidate primes to their smallest factor (if
     * they aren't prime) or 0 if they are prime.
     */ 
    private static Map<BigInteger, BigInteger> mPrimeCache
        = new ConcurrentHashMap<>();

    /**
     * Max number of iterations.
     */
    private static int sMAX_ITERATIONS = 10;

    /**
     * Duration for sleeping.
     */
    private static long sSLEEP_DURATION = 100;

    /**
     * The Java execution environment requires a static {@code main()}
     * entry point method to run the app.
     */
    static public void main(String[] argv) throws InterruptedException {
        testSequential();
        testConcurrent();
    }

    static private void testSequential() {
        System.out.println("begin testSequential()");

        Observable
                // Factor method that bridges the reactive world with the
                // callback-style world
                .create(ex03::emit)

                //
                .map(ex03::checkIfPrime)

                //
                .blockingSubscribe(ex03::processResult,
                        err -> print("ERROR" + err),
                        () -> print("DONE"));

        System.out.println("end testSequential()");
    }

    /**
     *
     */
    static private void testConcurrent() {
        System.out.println("begin testConcurrent()");
        Observable
                // Factor method that bridges the reactive world with the
                // callback-style world
                .create(ex03::emit)

                .observeOn(Schedulers.computation())
                //
                .map(ex03::checkIfPrime)

                //
                .subscribeOn(Schedulers.computation())

                //
                .blockingSubscribe(ex03::processResult,
                        err -> print("ERROR" + err),
                        () -> print("DONE"));

        System.out.println("end testConcurrent()");
    }

    /**
     *
     * @param emitter
     * @throws InterruptedException
     */
    private static void emit(ObservableEmitter<BigInteger> emitter) throws InterruptedException {
        new Random()
            // Generate random numbers between the min and max values
            // in a manner that ensures some duplicates.
            .ints(sMAX_ITERATIONS,
                 MAX_VALUE - sMAX_ITERATIONS,
                 MAX_VALUE)

            //
            .peek(randomNumber -> {
                ExceptionUtils.uncheck(() -> Thread.sleep(sSLEEP_DURATION));
                print("Emitting... " + randomNumber);
            })

            //
            .mapToObj(BigInteger::valueOf)

            // Emit the next random big integer.
            .forEach(emitter::onNext);

        // Shutdown the input stream.
        emitter.onComplete();
    }

    /**
     * This method ...
     */
    static PrimeTuple checkIfPrime(BigInteger primeCandidate) {
        return new PrimeTuple
                (primeCandidate,
                        mPrimeCache.computeIfAbsent(primeCandidate,
                                ex03::isPrime));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    static BigInteger isPrime(BigInteger n) {
        print("checking if " + n + " is prime");

        BigInteger two = BigInteger.valueOf(2);

        if (n.mod(two).compareTo(BigInteger.ZERO) == 0) 
            return two;          

        for (BigInteger i = BigInteger.valueOf(3);
             n.compareTo(i) >= 0;
             i = i.add(two)) 
            if (n.mod(i).compareTo(BigInteger.ZERO) == 0)
                return i;
    
        return BigInteger.ZERO;
    }
   
    /**
     *
     */
    private static void processResult(PrimeTuple primeTuple) {
        if (primeTuple.mSmallestFactor.equals(BigInteger.ZERO)) {
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
     *
     */
    private static class PrimeTuple {
        /**
         *
         */
        BigInteger mPrimeCandidate;

        /**
         *
         */
        BigInteger mSmallestFactor;

        /**
         *
         * @param primeCandidate
         * @param smallestFactor
         */
        public PrimeTuple(BigInteger primeCandidate, BigInteger smallestFactor) {
            mPrimeCandidate = primeCandidate;
            mSmallestFactor = smallestFactor;
        }
    }

    /**
     *
     */
    private static void print(String s) {
        System.out.println(s + " in thread "+ Thread.currentThread().getName());
    }
}

