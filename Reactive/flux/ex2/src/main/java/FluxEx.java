import org.reactivestreams.Subscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static utils.BigFractionUtils.sVoidM;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform various Flux operations, including
 * create(), interval(), map(), doOnNext(), take(), subscribe(), and
 * then().
 */
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
     * A memoizer cache that maps candidate primes to their smallest
     * factor (if they aren't prime) or 0 if they are prime.
     */ 
    private static final Map<BigInteger, BigInteger> mPrimeCache
        = new ConcurrentHashMap<>();

    /**
     * 100 milllisecond duration for sleeping.
     */
    private static final Duration sSLEEP_DURATION =
        Duration.ofMillis(100);

    /**
     * A factory method that bridges the reactive world with the
     * callback-style world to emit a flow of random big integers.
     */
    private static Consumer<FluxSink<BigInteger>> makeFluxSink(StringBuffer sb) {
        // Starting point of the randomly-generated numbers.
        final int lowerBound = sMAX_VALUE - sMAX_ITERATIONS;

        // Random number generator.
        final Random rand = new Random();

        return (FluxSink<BigInteger> sink) -> {
            Flux
                // Generate a big integer stream periodically in
                // a background thread.
                .interval(sSLEEP_DURATION)

                // Generate random numbers between min and max
                // values to ensure some duplicates.
                .map(x -> 
                     BigInteger.valueOf(lowerBound +
                                        rand.nextInt(sMAX_ITERATIONS)))

                // Print the big integer as a debugging aid.
                .doOnNext(s -> FluxEx.print(s, sb))

                // Only take sMAX_ITERATIONS of big integers.
                .take(sMAX_ITERATIONS)

                // Start the processing and emit each random number until
                // complete or an error occurs.
                .subscribe(sink::next,
                           // Shutdown the input stream on error.
                           error -> sink.complete(),
                           // Shutdown the input stream.
                           sink::complete);
        };
    }

    /**
     * Test a stream of random BigIntegers to determine which values
     * are prime using an asynchronous time-driven Flux stream.
     */
    public static Mono<Void> testIsPrime() {
        StringBuffer sb =
            new StringBuffer(">> Calling testIsPrime()\n");

        return Flux
            // Factor method creates a flow of random big integers
            // that are generated in a background thread.
            .create(makeFluxSink(sb))

            // Use a memoizer to check if each random big integer is
            // prime or not on the background thread.
            .map(bigInteger ->
                 FluxEx.checkIfPrime(bigInteger, sb))

            // Process each big integer on the background thread.
            .doOnNext(bigInteger ->
                       FluxEx.processResult(bigInteger,
                                            sb))

            // Display results after all elements in flux stream are processed
            // and return an empty mono to synchronize with AsyncTester.
            .then(Mono.fromRunnable(() ->
                                    BigFractionUtils.display(sb.toString())));
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
             mPrimeCache
             .computeIfAbsent(primeCandidate,
                              pc -> (FluxEx.isPrime(pc,
                                                    sb))));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime or the smallest factor if it is not prime.
     */
    static BigInteger isPrime(BigInteger n,
                              StringBuffer sb) {
        print("checking if " + n + " is prime", sb);

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
