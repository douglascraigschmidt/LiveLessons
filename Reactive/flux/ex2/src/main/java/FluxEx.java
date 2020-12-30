import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform various Flux operators, including
 * create(), interval(), map(), filter(), doOnNext(), take(),
 * subscribe(), then(), range(), subscribeOn(), publishOn(), and
 * various thread pools.  The Mono fromRunnable() operator is also
 * shown.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Maximum random number value.
     */
    private static final int sMAX_VALUE = 10_000_000;

    /**
     * Max number of iterations.
     */
    private static final int sMAX_ITERATIONS = 10;

    /**
     * Starting point of the randomly-generated numbers.
     */
    private static final int sLOWER_BOUND = sMAX_VALUE - sMAX_ITERATIONS;

    /**
     * Random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * A memoizer cache that maps candidate primes to their smallest
     * factor (if they aren't prime) or 0 if they are prime.
     */ 
    private static final Map<BigInteger, BigInteger> mPrimeCache
        = new ConcurrentHashMap<>();

    /**
     * 0.5 second duration for sleeping.
     */
    private static final Duration sSLEEP_DURATION =
        Duration.ofMillis(500);

    /**
     * Define a predicate that only matches odd numbers.
     */
    private static Predicate<BigInteger> sOnlyOdd = bigInteger ->
        !bigInteger.mod(BigInteger.TWO).equals(BigInteger.ZERO);

    /**
     * Generate a random BigInteger.
     */
    private static Function<Long, BigInteger> sGenerateRandomBigInteger = __ ->
        BigInteger.valueOf(sLOWER_BOUND +
                           sRANDOM.nextInt(sMAX_ITERATIONS));

    /**
     * Test a stream of random BigIntegers to determine which values
     * are prime using an asynchronous time-driven Flux stream.
     */
    public static Mono<Void> testIsPrimeTimed() {
        StringBuffer sb =
            new StringBuffer(">> Calling testIsPrimeTimed()\n");

        // Callback that writes the BigInteger to the StringBuffer.
        Consumer<BigInteger> logBigInteger =
            s -> FluxEx.print(s, sb);

        return Flux
            // Factory method creates a flow of random big integers
            // that are generated at a periodic interval in a
            // background thread.
            .create(makeTimedFluxSink())

            // Print the big integer as a debugging aid.
            .doOnNext(logBigInteger)

            // Use a memoizer to check if each random big integer is
            // prime or not on the background thread.
            .map(bigInteger ->
                 FluxEx.checkIfPrime(bigInteger, sb))

            // Process each big integer on the background thread.
            .doOnNext(bigInteger ->
                       FluxEx.processResult(bigInteger,
                                            sb))

            // Display results after all elements in flux stream are
            // processed and return an empty mono to synchronize with
            // AsyncTaskBarrier.
            .then(Mono.fromRunnable(() ->
                                    BigFractionUtils.display(sb.toString())));
    }

    /**
     * A factory method that bridges the reactive world with the
     * callback-style world to emit a time-based flow of random big
     * integers at a periodic interval.
     */
    private static Consumer<FluxSink<BigInteger>> makeTimedFluxSink() {
        // FluxSink emits any number of next() signals followed by
        // zero or one onError()/onComplete().
        return (FluxSink<BigInteger> sink) -> Flux
            // Generate a big integer stream periodically in a
            // background thread.
            .interval(sSLEEP_DURATION)

            // Generate random numbers between min and max values to
            // ensure some duplicates.
            .map(sGenerateRandomBigInteger)

            // Eliminate even numbers from consideration since they
            // aren't prime!
            .filter(sOnlyOdd)

            // Only take sMAX_ITERATIONS of big integers.
            .take(sMAX_ITERATIONS)

            // Start the processing and emit each random number until
            // complete or an error occurs.
            .subscribe(sink::next,
                       // Shutdown the input stream on error.
                       error -> sink.complete(),
                       // Shutdown the input stream.
                       sink::complete);
    }

    /**
     * Test a stream of random BigIntegers to determine which values
     * are prime using an asynchronous Flux stream.
     */
    public static Mono<Void> testIsPrimeAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testIsPrimeAsync()\n");

        // Callback that writes the BigInteger to the StringBuffer.
        Consumer<BigInteger> logBigInteger =
            s -> FluxEx.print(s, sb);

        return Flux
            // Factory method creates a stream of random big integers
            // that are generated in a background thread.
            .create(makeAsyncFluxSink())

            // Print the big integer as a debugging aid.
            .doOnNext(logBigInteger)

            // Arrange to perform the prime-checking computations in
            // the "subscriber" thread.
            .publishOn(Schedulers.newParallel("subscriber", 1))

            // Use a memoizer to check if each random big integer is
            // prime or not in the "subscriber" thread.
            .map(bigInteger ->
                 FluxEx.checkIfPrime(bigInteger, sb))

            // Process each big integer in the "subscriber" thread.
            .doOnNext(bigInteger ->
                       FluxEx.processResult(bigInteger,
                                            sb))

            // Display results after all elements in the flux stream are
            // processed and return an empty mono to synchronize with
            // AsyncTaskBarrier.
            .then(Mono.fromRunnable(() ->
                                    BigFractionUtils.display(sb.toString())));
    }

    /**
     * A factory method that bridges the reactive world with the
     * callback-style world to emit a flow of random big integers in a
     * background thread.
     */
    private static Consumer<FluxSink<BigInteger>> makeAsyncFluxSink() {
        // FluxSink emits any number of next() signals followed by
        // zero or one onError()/onComplete().
        return (FluxSink<BigInteger> sink) -> Flux
            // Emit sMAX_ITERATIONS integers starting at 1.
            .range(1, sMAX_ITERATIONS)

            // Convert to Long since Flux lacks a rangeLong() method.
            .map(Integer::toUnsignedLong)

            // Arrange to emit the random big integers in the
            // "publisher" thread.
            .subscribeOn(Schedulers.newParallel("publisher", 1))

            // Generate random numbers between min and max values
            // to ensure some duplicates.
            .map(sGenerateRandomBigInteger)

            // Eliminate even numbers from consideration since they
            // aren't prime!
            .filter(sOnlyOdd)

            // Start the processing and emit each random number until
            // complete or an error occurs.
            .subscribe(sink::next,
                       // Shutdown the input stream on error.
                       error -> sink.complete(),
                       // Shutdown the input stream.
                       sink::complete);
    }

    /**
     * This method checks whether the {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check for the prime factor.
     * @return a PrimeResult that contains the prime candidate and
     *         either 0 (if the prime candidate is prime) or the
     *         smallest factor (if it's not prime)
     */
    static PrimeResult checkIfPrime(BigInteger primeCandidate,
                                    StringBuffer sb) {
        return new PrimeResult
            (primeCandidate,
             // This atomic "check then act" method serves as
             // a "memoizer" cache.
             mPrimeCache.computeIfAbsent(primeCandidate,
                                         pc -> (FluxEx.isPrime(pc, sb))));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime or the smallest factor if it is not prime.
     */
    static BigInteger isPrime(BigInteger n,
                              StringBuffer sb) {
        print("checking if " + n + " is prime",
                sb);

        // Even numbers can't be prime.
        if (n.mod(BigInteger.TWO).compareTo(BigInteger.TWO) == 0)
            return BigInteger.TWO;

        for (BigInteger i = BigInteger.valueOf(3);
             n.compareTo(i.multiply(i)) >= 0;
             i = i.add(BigInteger.TWO))
            if (n.mod(i).compareTo(BigInteger.ZERO) == 0)
                return i;
    
        return BigInteger.ZERO;
    }

    /**
     * Process the {@code primeTuple} to print whether a number if prime.
     */
    private static void processResult(PrimeResult primeTuple,
                                      StringBuffer sb) {
        if (!primeTuple.mSmallestFactor.equals(BigInteger.ZERO)) {
            print("found a non-prime number with smallest factor "
                  + primeTuple.mSmallestFactor
                  + " for "
                  + primeTuple.mPrimeCandidate, sb);
        } else {
            print("found a prime number "
                  + primeTuple.mPrimeCandidate, sb);
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
    private static void print(String s, StringBuffer sb) {
        sb.append("["
                  + Thread.currentThread().getName()
                  + ", "
                  + System.currentTimeMillis()
                  + "] "
                  + s
                  + "\n");
    }

    /**
     * Print big integer {@code bigInteger} with the thread name appended.
     */
    private static void print(BigInteger bigInteger, StringBuffer sb) {
        print("emitting " + bigInteger, sb);
    }
}
