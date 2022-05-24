import atomiclongs.*;
import utils.RunTimer;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

/**
 * This program demonstrates various ways to implement an "AtomicLong"
 * class, including using Java StampedLock, ReentrantReadWriteLock,
 * synchronized statements, and VarHandle.
 */
class ex39 {
    /**
     * Number of iterations to run the tests.
     */
    private static final int sMAX_ITERATIONS = 100_000_000;

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
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
    private static void runTest(AtomicLong atomicLong,
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
    private static void testAtomicLong(AtomicLong atomicLong,
                                       String testName) {
        System.out.println("Entering " + testName);

        Phaser entryBarrier = new Phaser();

        // Create four threads that run various AtomicLong methods.
        Thread[] threads = {
            new Thread(makeRunnable(entryBarrier,
                                    atomicLong::incrementAndGet)),
            new Thread(makeRunnable(entryBarrier,
                                    atomicLong::decrementAndGet)),
            new Thread(makeRunnable(entryBarrier,
                                    atomicLong::getAndIncrement)),
            new Thread(makeRunnable(entryBarrier,
                                    atomicLong::getAndDecrement)) /* ,
            new Thread(makeRunnable(entryBarrier,
                        atomicLong::get)),
            new Thread(makeRunnable(entryBarrier,
                        atomicLong::get)),
                new Thread(makeRunnable(entryBarrier,
                        atomicLong::get)),
                new Thread(makeRunnable(entryBarrier,
                        atomicLong::get)) */
        };

        entryBarrier.bulkRegister(threads.length);

        // Start all the threads.
        for (Thread thread : threads)
            thread.start();

        try {
            // Wait for all the threads to exit.
            for (Thread thread : threads)
                thread.join();
        } catch (Exception ignored) {
        }

        System.out.println("Leaving " 
                           + testName
                           + " with AtomicLong value = " 
                           + atomicLong.get());
    }

    private static Runnable makeRunnable(Phaser entryBarrier,
                                         Runnable runnable) {
        return () -> {
            entryBarrier.arriveAndAwaitAdvance();

            for (int i = 0; i < sMAX_ITERATIONS; ++i)
                runnable.run();
        };
    }
}
