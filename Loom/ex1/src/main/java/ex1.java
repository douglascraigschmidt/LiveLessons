import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.function.IntConsumer;
import java.util.Random;

import utils.RunTimer;
import utils.Options;
import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowSupplier;

/**
 * This example demonstrates how to create, start, and use virtual and
 * platform Thread objects in Project Loom, which is exploring and
 * incubating Java VM features and APIs built on top of them for the
 * implementation of lightweight user-mode threads (virtual threads).
 * You'll need to install JDK 18 with Project Loom configured, which
 * you can download from https://jdk.java.net/loom.
 */
public class ex1 {
    /**
     * A List of randomly-generated integers.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {

        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Generate the random numbers.
        generateRandomNumbers();

        // Create/start the threads with the given option.
        startThreads(Options.instance().virtualThreads());
    }

    /**
     * Generate a list of random Integer objects used for prime number
     * checking.
     */
    private static void generateRandomNumbers() {
        // Generate a list of random integers.
        sRANDOM_INTEGERS = new Random()
            // Generate the given # of large random ints.
            .ints(Options.instance().numberOfElements(),
                  Integer.MAX_VALUE - Options.instance().numberOfElements(),
                  Integer.MAX_VALUE)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into a
            // List.
            .collect(toList());
    }

    /**
     * Demonstrate how to create and start many threads using Project
     * Loom.
     *
     * @param virtual If true create a virtual thread, else create a
     *                platform thread
     */
    private static void startThreads(boolean virtual) {
        // Print out a diagnostic every 1000 ints.
        IntConsumer printDiagnostic = i -> {
            if (i % 1000 == 0)
                System.out.println(i + " threads started");
        };

        // Create a List of many Thread objects.
        List<Thread> threads = IntStream
            // Generate a range of ints.
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
            .collect(toList());

        // Start all the Thread objects.
        threads.forEach(Thread::start);

        // Join all the Thread objects (barrier synchronizer).
        threads.forEach(rethrowConsumer(Thread::join));
    }

    /**
     * This factory method creates and returns a new unstarted {@link
     * Thread} (either virtual or platform) that will run the given
     * {@code runnable} after the {@link Thread} is started.
     *
     * @param runnable The {@link Runnable} to run in the new {@link
     *                 Thread}
     * @param virtual If true the {@link Thread} should be a virtual
     *        thread, else it should be a platform thread
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
        return () -> isPrime(integer);
    }

}
