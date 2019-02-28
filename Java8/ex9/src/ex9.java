import utils.RunTimer;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * This example showcases the use of a Java 8 ConcurrentHashMap and a
 * Java SynchronizedMap together with Java 8 Function-based method
 * references to compute/cache/retrieve prime numbers.
 */
public class ex9 {
    /**
     * Number of times each thread iterates computing prime numbers.
     */
    private static int sMAX = 100000;

    /**
     * True if we're running in verbose mode, else false.
     */
    private static boolean sVERBOSE = false;

    /**
     * Number of cores known to the Java execution environment.
     */
    private static int sNUMBER_OF_CORES =
        Runtime.getRuntime().availableProcessors();

    /**
     * This executor runs the prime number computation tasks.
     */
    private ExecutorService mExecutor =
        // Create a pool with as many threads as the Java execution
        // environment thinks there are cores.
        Executors.newFixedThreadPool(sNUMBER_OF_CORES);

    /**
     * This method provides a brute-force determination of whether
     * number @a primeCandidate is prime.  Returns 0 if it is prime,
     * or the smallest factor if it is not prime.
     */
    private Integer isPrime(Integer primeCandidate) {
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
     * Run the prime number test.
     * 
     * @param maxIterations Number of iterations to run the test
     * @param primeCache Cache that maps candidate primes to their
     * smallest factor (if they aren't prime) or 0 if they are prime
     * @param testName Name of the test
     */
    private void runTest(int maxIterations,
                         Map<Integer, Integer> primeCache,
                         String testName) {
        try {
            System.out.println("Starting " + testName);

            // Exit barrier that keeps track of when the tasks finish.
            CountDownLatch exitBarrier =
                new CountDownLatch(sNUMBER_OF_CORES);

            // Random number generator.
            Random random = new Random();

            // Runnable checks if maxIterations random numbers are prime.
            Runnable isPrime = () -> {
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
                                               this::isPrime);

                    if (smallestFactor != 0) {
                        if (sVERBOSE)
                            System.out.println(""
                                               + Thread.currentThread()
                                               + ": "
                                               + primeCandidate
                                               + " is not prime with smallest factor "
                                               + smallestFactor);
                    } else {
                        if (sVERBOSE)
                            System.out.println(""
                                               + Thread.currentThread()
                                               + ": "
                                               + primeCandidate
                                               + " is prime");
                    }
                }

                // Inform the waiting thread that we're done.
                exitBarrier.countDown();
            };

            // Create a task running the prime checker algorithm.
            for (int i = 0; i < sNUMBER_OF_CORES; i++)
                mExecutor.execute(isPrime);

            // Wait until we're done.
            exitBarrier.await();

            System.out.println("Leaving " + testName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        // Time how long this test takes to run.
        RunTimer.timeRun(() 
                         // Run the test using a ConcurrentHashMap.
                         -> test.runTest(maxIterations,
                                            new ConcurrentHashMap<>(),
                                            "ConcurrentHashMap"),
                         "ConcurrentHashMap");

        // Time how long this test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test using a synchronized HashMap.
                         test.runTest(maxIterations,
                                      Collections.synchronizedMap(new HashMap<>()),
                                      "SynchronizedHashMap"),
                         "SynchronizedHashMap");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        // Shutdown the executor.
        test.mExecutor.shutdownNow();
    }
}
    
