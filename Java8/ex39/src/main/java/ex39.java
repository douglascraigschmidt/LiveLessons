import atomiclongs.*;
import utils.ExceptionUtils;
import utils.RunTimer;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;
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
     * Number of iterations to run each test.
     */
    private static final int sMAX_ITERATIONS = 10_000_000;

    /**
     * The total number of readers.
     */
    private static final int sREADERS = 20;

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Run the tests that exercise all the AtomicLong
        // implementations.
        runTest(new AtomicLongRWL(1),
                "AtomicLongRWL");
        runTest(new AtomicLongStampedLock(1),
                "AtomicLongStampedLock");
        runTest(new AtomicLongSync(1),
                "AtomicLongSync");
        runTest(new AtomicLongVarHandle(1),
                "AtomicLongVarHandle");
        runTest(new AtomicLongAdapter(1),
                "AtomicLong");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Time the various {@link AbstractAtomicLong} implementations.
     *
     * @param abstractAtomicLong The {@link AbstractAtomicLong} implementation
     */
    private static void runTest(AbstractAtomicLong abstractAtomicLong,
                                String testName) {
        // Let the system garbage collect before running the test.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testAtomicLong(abstractAtomicLong,
                                        testName),
                         testName);
    }

    /**
     * Test the various {@link AbstractAtomicLong} implementations.
     *
     * @param abstractAtomicLong The {@link AbstractAtomicLong} implementation
     */
    private static void testAtomicLong(AbstractAtomicLong abstractAtomicLong,
                                       String testName) {
        System.out.println("Entering " + testName);

        // Create an entry barrier.
        var entryBarrier = new Phaser();

        // Create threads that run various AtomicLong methods.
        List<Thread> threads = new ArrayList<>();

        // Create the reader threads.
        makeReaders(threads, abstractAtomicLong, entryBarrier, sREADERS);

        // Create the writer threads.
        makeWriters(threads, abstractAtomicLong, entryBarrier);

        // Register all the Thread objects.
        entryBarrier.bulkRegister(threads.size());

        // Start all the threads.
        threads.forEach(Thread::start);

        // Barrier synchronization that waits for all the threads to exit.
        threads.forEach(ExceptionUtils.rethrowConsumer(Thread::join));

        // Ensure the AtomicLong implementation worked properly.
        assert abstractAtomicLong.get() == 1;

        System.out.println("Leaving " 
                           + testName
                           + " with AtomicLong value = " 
                           + abstractAtomicLong.get());
    }

    /**
     * This factory method creates the writer {@link Thread} objects.
     *
     * @param threads The {@link List} of {@link Thread} objects
     * @param abstractAtomicLong The {@link AbstractAtomicLong} implementation
     * @param entryBarrier The {@link Phaser} that serves as an entry
     *                     barrier
     */
    private static void makeWriters(List<Thread> threads,
                                    AbstractAtomicLong abstractAtomicLong,
                                    Phaser entryBarrier) {
        threads
            // Create and add all the writer Thread objects.
            .addAll(List
                    .of(new Thread
                        (makeRunnable(entryBarrier,
                                      abstractAtomicLong::incrementAndGet)),
                        new Thread
                        (makeRunnable(entryBarrier,
                                      abstractAtomicLong::decrementAndGet)),
                        new Thread
                        (makeRunnable(entryBarrier,
                                      abstractAtomicLong::getAndIncrement)),
                        new Thread
                        (makeRunnable(entryBarrier,
                                      abstractAtomicLong::getAndDecrement))));
    }

    /**
     * This factory method creates the reader {@link Thread} objects.
     *
     * @param threads The {@link List} of {@link Thread} objects
     * @param abstractAtomicLong The {@link AbstractAtomicLong} implementation
     * @param entryBarrier The {@link Phaser} that serves as an entry
     *                     barrier
     */
    private static void makeReaders(List<Thread> threads,
                                    AbstractAtomicLong abstractAtomicLong,
                                    Phaser entryBarrier,
                                    int readers) {
        // Create the given number of reader Thread objects.
        for (int i = 0; i < readers; i++)
            threads.add(new Thread(makeRunnable(entryBarrier,
                                                abstractAtomicLong::get)));
    }

    /**
     * This factory method returns a {@link Runnable} that performs the task.
     *
     * @param entryBarrier The {@link Phaser} that serves as an entry
     *                     barrier
     * @param runnable The task to perform
     * @return A {@link Runnable} that performs the task
     */
    private static Runnable makeRunnable(Phaser entryBarrier,
                                         Runnable runnable) {
        return () -> {
            // Wait for all the threads to arrive.
            entryBarrier.arriveAndAwaitAdvance();

            // Perform the task for the designated number of
            // iterations.
            for (int i = 0; i < sMAX_ITERATIONS; ++i)
                runnable.run();
        };
    }
}
