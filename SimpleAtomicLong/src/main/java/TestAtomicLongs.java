import atomiclongs.*;
import utils.RunTimer;

class TestAtomicLongs {
    private static int sMAX_ITERATIONS = 10_000_000;

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
     * @param al The {@link AtomicLong} implementation.
     */
    private static void runTest(AtomicLong al,
                                String testName) {
        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testAtomicLong(al,
                                        testName),
                         testName);
    }

    /**
     * Test the various {@link AtomicLong} implementations.
     *
     * @param al The {@link AtomicLong} implementation.
     */
    private static void testAtomicLong(AtomicLong al,
                                       String testName) {
        System.out.println("Entering " + testName);

        Thread t1 = new Thread(() -> {
                for (int i = 0; i < sMAX_ITERATIONS; ++i)
                    al.incrementAndGet();
        });

        Thread t2 = new Thread(() -> {
                for (int i = 0; i < sMAX_ITERATIONS; ++i)
                    al.decrementAndGet();
        });

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (Exception ignored) {
        }

        System.out.println("Leaving " 
                           + testName
                           + " with AtomicLong value = " 
                           + al.get());
    }
}
