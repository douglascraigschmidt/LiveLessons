import atomiclongs.*;
import utils.ExceptionUtils;
import utils.RunTimer;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * This program demonstrates various ways to implement an "AtomicLong"
 * class, including using Java {@link StampedLock}, {@link
 * ReentrantReadWriteLock}, synchronized statements, and {@link
 * VarHandle}.
 */
class ex39 {
    /**
     * Number of iterations to run the tests.
     */
    private static final int sMAX_ITERATIONS = 100_000_000;

    /**
     * A count of the number of readers.
     */
    private static final int sREADERS = 4;

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        new ex39();
    }

    public ex39() {
        runTest(new AtomicLongRWL(1), "AtomicLongRWL");
        runTest(new AtomicLongStampedLock(1), "AtomicLongStampedLock");
        runTest(new AtomicLongSync(1), "AtomicLongSync");
        runTest(new AtomicLongVarHandle(1), "AtomicLongVarHandle");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Time the various {@link AtomicLong} implementations.
     *
     * @param atomicLong The {@link AtomicLong} implementation
     */
    private void runTest(AtomicLong atomicLong,
                         String testName) {
        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testAtomicLong(atomicLong,
                                        testName),
                         testName);
    }

    /**
     * Test the various {@link AtomicLong} implementations.
     *
     * @param atomicLong The {@link AtomicLong} implementation
     */
    private void testAtomicLong(AtomicLong atomicLong,
                                String testName) {
        System.out.println("Entering " + testName);

        // Create an entry barrier.
        Phaser entryBarrier = new Phaser();

        // Create threads that run various AtomicLong methods.
        List<Thread> threads = new ArrayList<>();

        // Create the reader threads.
        makeReaders(threads, atomicLong, entryBarrier, sREADERS);

        // Create the writer threads.
        makeWriters(threads, atomicLong, entryBarrier);

        entryBarrier.bulkRegister(threads.size());

        // Start all the threads.
        threads.forEach(Thread::start);

        // Barrier synchronization that waits for all the threads to exit.
        threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

        System.out.println("Leaving " 
                           + testName
                           + " with AtomicLong value = " 
                           + atomicLong.get());
    }

    /**
     * This factory method creates the writer threads.
     *
     * @param threads The {@link List} of {@link Thread} objects
     * @param atomicLong The {@link AtomicLong} implementation
     * @param entryBarrier The {@link Phaser} that serves as an entry barrier
     */
    private void makeWriters(List<Thread> threads,
                             AtomicLong atomicLong,
                             Phaser entryBarrier) {
        // Create and add all the writer tasks.
        threads
            .addAll(List
                    .of(new Thread(makeRunnable(entryBarrier,
                                                atomicLong::incrementAndGet)),
                        new Thread(makeRunnable(entryBarrier,
                                                atomicLong::decrementAndGet)),
                        new Thread(makeRunnable(entryBarrier,
                                                atomicLong::getAndIncrement)),
                        new Thread(makeRunnable(entryBarrier,
                                                atomicLong::getAndDecrement))));
    }

    /**
     * This factory method creates the reader threads.
     *
     * @param threads The {@link List} of {@link Thread} objects
     * @param atomicLong The {@link AtomicLong} implementation
     * @param entryBarrier The {@link Phaser} that serves as an entry barrier
     */
    private void makeReaders(List<Thread> threads,
                             AtomicLong atomicLong,
                             Phaser entryBarrier,
                             int readers) {
        // Create the given number of reader tasks.
        for (int i = 0; i < readers; i++)
            threads.add(new Thread(makeRunnable(entryBarrier,
                                                atomicLong::get)));
    }

    /**
     * This factory method returns a {@link Runnable} that performs the task.
     *
     * @param entryBarrier The {@link Phaser} that serves as an entry barrier
     * @param runnable The task to perform
     * @return A {@link Runnable} that performs the task
     */
    private Runnable makeRunnable(Phaser entryBarrier,
                                  Runnable runnable) {
        return () -> {
            // Wait for all the threads to arrive.
            entryBarrier.arriveAndAwaitAdvance();

            // Perform the task for the designated number
            // of iterations.
            for (int i = 0; i < sMAX_ITERATIONS; ++i)
                runnable.run();
        };
    }
}
