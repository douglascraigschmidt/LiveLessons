import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This example showcases the use of a Java 8 ConcurrentHashMap and a
 * Java SynchronizedMap together with Java 8 Function-based method
 * references to compute/cache/retrieve prime numbers.
 */
public class ex9 {
    /**
     * Number of times each thread iterates computing prime numbers.
     */
    private static int sMAX = 1000;

    /**
     * This method provides a brute-force determination of whether
     * number @a primeCandidate is prime.  Returns 0 if it is prime,
     * or the smallest factor if it is not prime.
     */
    private Integer primeChecker(Integer primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    System.out.println(""
                                       + Thread.currentThread()
                                       + " Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * Run the prime number test using a ConcurrentHashMap.
     */
    private void runConcurrentHashMapTest(int maxIterations)
        throws InterruptedException {
        System.out.println("Starting runConcurrentHashMapTest");

        // Random number generator.
        final Random random = 
            new Random();

        // Cache that maps candidate primes to their smallest factor
        // (if they aren't prime) or 0 if they are prime.
        final Map<Integer, Integer> primeCache =
            new ConcurrentHashMap<>();

        // Runnable checks if maxIterations random numbers are prime.
        Runnable primeChecker = () -> {
            for (long l = 0; l < maxIterations; l++) {
                // Get the next random number.
                Integer primeCandidate = 
                Math.abs(random.nextInt(maxIterations) + 1);

                // computeIfAbsent() first checks to see if the factor
                // for this number is already in the cache.  If not,
                // it atomically determines if this number is prime
                // and stores it in the cache.
                Integer smallestFactor =
                primeCache.computeIfAbsent(primeCandidate,
                                           this::primeChecker);

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

        // Create a list of threads, each running the prime checker
        // algorithm.
        List<Thread> threads =
            new ArrayList<>(Arrays.asList(new Thread(primeChecker),
                                          new Thread(primeChecker),
                                          new Thread(primeChecker)));

        // Start all the threads.
        threads.forEach(Thread::start);

        // Wait for all the threads to finish.
        for (Thread thread : threads)
            thread.join();

        System.out.println("Leaving runConcurrentHashMapTest");
    }

    /**
     * Run the prime number test using a SynchronizedMap.
     */
    private void runSynchronizedMapTest(int maxIterations)
        throws InterruptedException {
        System.out.println("Starting runSynchronizedMapTest");

        // Random number generator.
        final Random random = 
            new Random();

        // Cache that maps candidate primes to their smallest factor
        // (if they aren't prime) or 0 if they are prime.
        final Map<Integer, Integer> primeCache =
            Collections.synchronizedMap(new HashMap<>());

        // Runnable checks if maxIterations random numbers are prime.
        Runnable primeChecker = () -> {
            for (long l = 0; l < maxIterations; l++) {
                // Get the next random number.
                Integer primeCandidate = 
                Math.abs(random.nextInt(maxIterations) + 1);

                // computeIfAbsent() first checks to see if the factor
                // for this number is already in the cache.  If not,
                // it atomically determines if this number is prime
                // and stores it in the cache.
                Integer smallestFactor =
                primeCache.computeIfAbsent(primeCandidate,
                                           this::primeChecker);

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

        // Create a list of threads, each running the prime checker
        // algorithm.
        List<Thread> threads =
            new ArrayList<>(Arrays.asList(new Thread(primeChecker),
                                          new Thread(primeChecker),
                                          new Thread(primeChecker)));

        // Start all the threads.
        threads.forEach(Thread::start);

        // Wait for all the threads to finish.
        for (Thread thread : threads)
            thread.join();

        System.out.println("Leaving runSynchronizedMapTest");
    }

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) throws InterruptedException {
        // Determine the max number of iterations.
        int maxIterations = argv.length == 0 
            ? sMAX 
            : Integer.valueOf(argv[0]);

        ex9 test = new ex9();
        
        test.runConcurrentHashMapTest(maxIterations);
        test.runSynchronizedMapTest(maxIterations);
    }
}

