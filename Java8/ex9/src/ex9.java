import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This example uses a Java 8 ConcurrentHashMap and Java 8 Function-based method reference cache prime numbers.
 */
public class ex9 {
    /**
     * This method provides a brute-force determination of whether
     * number @a primeCandidate is prime.  Returns 0 if it is prime, or the
     * smallest factor if it is not prime.
     */
    private static Long primeChecker(Long primeCandidate) {
        long n = primeCandidate;

        if (n > 3)
            for (long factor = 2;
                 factor <= n / 2;
                 ++factor)
                if ((factor % (n / 1000)) == 0
                    && Thread.interrupted()) {
                    System.out.println(""
                                       + Thread.currentThread()
                                       + " Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0L;
    }

    /**
     * Number of times each thread iterates computing prime numbers.
     */
    private static long sMAX = 1000;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Determine the max number of iterations.
        long maxIterations = argv.length == 0 ? sMAX : Long.valueOf(argv[0]);

        // Random number generator.
        final Random random = 
            new Random();

        // Cache that maps candidate primes to their smallest factor (if they aren't prime) or 0 if they are prime.
        final ConcurrentMap<Long, Long> primeCache =
            new ConcurrentHashMap<>();

        // This runnable checks to see if sMAX random numbers are prime.
        Runnable primeChecker = () -> {
            for (long l = 0; l < maxIterations; l++) {
                // Get the next random number.
                Long primeCandidate = random.nextLong();

                // Check to see if the factor for this number is already in the cache.
                Long smallestFactor = primeCache.get(primeCandidate);

                if (smallestFactor == null)
                    // If not, then atomically determine if this number is prime and store it in the cache.
                    smallestFactor = primeCache.computeIfAbsent
                        (primeCandidate, ex9::primeChecker);

                if (smallestFactor != 0)
                    System.out.println(""
                                       + Thread.currentThread()
                                       + ": "
                                       + primeCandidate
                                       + " is not prime with smallest factor "
                                       + smallestFactor);
                else
                    System.out.println(""
                                       + Thread.currentThread()
                                       + ": "
                                       + primeCandidate
                                       + " is prime");
            }
        };

        // Create a list of threads each of which running the prime checker algorithm.
        List<Thread> threads =
            new ArrayList<>(Arrays.asList(new Thread(primeChecker),
                                          new Thread(primeChecker),
                                          new Thread(primeChecker)));

        // Start all the threads.
        threads.forEach(Thread::start);

        // Wait for all the threads to finish.
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}

