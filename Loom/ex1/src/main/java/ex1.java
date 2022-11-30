import utils.MathUtils;
import utils.Options;
import utils.RandomUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static utils.ExceptionUtils.rethrowConsumer;

/**
 * This example demonstrates how to create, start, and use virtual and
 * platform Thread objects in Project Loom, which is exploring and
 * incubating Java VM features and APIs built on top of them for the
 * implementation of lightweight user-mode threads (virtual threads).
 * You'll need to install JDK 19 with gradle 7.6 configured.
 */
public class ex1 {
    /**
     * A {@link List} of randomly-generated {@link Integer} objects.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Keeps track of the number of iterations.
     */
    private static final AtomicInteger sIterationCount =
        new AtomicInteger();

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {

        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        System.out.println("Entering test with "
                          + Options.instance().numberOfElements()
                          + (Options.instance().virtualThreads()
                             ? " virtual" : " platform")
                           + " threads");

        // Generate a List of random integers.
        sRANDOM_INTEGERS = RandomUtils
            .generateRandomNumbers(Options.instance().numberOfElements(),
                                   Integer.MAX_VALUE);

        // Create/start the threads with the given option to either
        // create virtual or platform threads.
        startThreads(Options.instance().virtualThreads());

        System.out.println("Leaving test");
    }

    /**
     * Demonstrate how to create and start many platform or virtual
     * Java {@link Thread} objects.
     *
     * @param virtual If true create a virtual {@link Thread}, else
     *                create a platform {@link Thread}
     */
    private static void startThreads(boolean virtual) {
        // Print out a diagnostic periodically.
        IntConsumer printDiagnostic = i -> {
            if (Options.instance().printDiagnostic(i))
                System.out.println(i + " threads started");
        };

        // Create a List of many Thread objects.
        List<Thread> threads = IntStream
            // Generate stream containing a range of ints.
            .rangeClosed(1, Options.instance().numberOfElements())

            // Print a helpful diagnostic.
            .peek(printDiagnostic)

            // Convert type of stream from IntStream to Stream<T>.
            .mapToObj(i ->
                      // Make a new Thread (either virtual or
                      // platform) for each int and give it a large
                      // random number to check for primality.
                      makeThread(makeRunnable(sRANDOM_INTEGERS.get(i - 1)),
                                 virtual))

            // Trigger intermediate processing and collect the Thread
            // objects into a List.
            .toList();

        // Start all the Thread objects.
        threads.forEach(Thread::start);

        // Join all the Thread objects (barrier synchronizer).
        threads.forEach(rethrowConsumer(Thread::join));
    }

    /**
     * This factory method creates and returns a new unstarted {@link
     * Thread} (either virtual or platform) that will run the given
     * {@link Runnable} after the {@link Thread} starts.
     *
     * @param runnable The {@link Runnable} to run in a new {@link
     *                 Thread}
     * @param virtual If true the {@link Thread} should be a virtual
     *        {@link Thread}, else it should be a platform {@link
     *        Thread}
     */
    public static Thread makeThread(Runnable runnable,
                                    boolean virtual) {
        if (virtual)
            // Create a virtual thread, which is multiplexed atop
            // worker threads in a Java fork-join pool.
            return Thread.ofVirtual().unstarted(runnable);
        else
            // Create a platform thread, i.e., 1-to-1 mapping with an
            // OS thread.
            return Thread.ofPlatform().unstarted(runnable);
    }

    /**
     * This factory method returns a new {@link Runnable} that checks
     * the given {@code integer} for primality.
     *
     * @param integer The number to check for primality
     * @return A {@link Runnable} that checks {@code integer} for
     *         primality
     */
    public static Runnable makeRunnable(int integer) {
        // Return a Runnable lambda that checks if integer is prime.
        return () -> {
            // Determine if integer is prime or not.
            var result = MathUtils.isPrime(integer);

            // Periodically print the result of checking for
            // primality.
            if (Options.instance()
                .printDiagnostic(sIterationCount.getAndIncrement())) {
                if (result == 0)
                    System.out.println(integer + " is prime");
                else
                    System.out.println(integer
                                       + " is not prime with smallest factor "
                                       + result);
            }
        };
    }
}
