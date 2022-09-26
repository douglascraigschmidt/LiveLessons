package edu.vandy;

import static org.junit.Assert.assertEquals;

import org.junit.*;

/**
 * This example illustrates how deadlock can occur due to "circular
 * waiting".  Moreover, deadlocks occur sporadically, which makes it
 * even harder to identify and diagnose the problem!
 */
public class DeadlockTest {
    /**
     * This helper class is passed a parameter to a Java thread.
     */
    static class TransferRunnable implements Runnable {
        /**
         * First instance of the SimpleQueue.
         */
        private final SimpleQueue<String> mAQueue;

        /**
         * Second instance of the SimpleQueue.
         */
        private final SimpleQueue<String> mBQueue;

        /**
         * Number of iterations to run the test.
         */
        private final int mIterations;

        /**
         * Constructor stores the parameters into data members.
         */
        public TransferRunnable(SimpleQueue<String> a,
                                SimpleQueue<String> b,
                                int iterations) {
            mAQueue = a;
            mBQueue = b;
            mIterations = iterations;
        }

        /**
         * This hook method is called in a new Thread and it transfers
         * the contents of mAQueue to mBQueue.
         */
        @Override
        public void run() {
            try {
                for (int i = 0; i < mIterations; ++i)
                    TransferRunnable.transfer(mAQueue,
                                              mBQueue);
            } catch (Exception e) {
                System.out.println("caught exception");
            }
        }

        /**
         * Copies contents of src to dest in a synchronized manner
         * since SimpleQueue lacks have internal synchronization.
         */
        public static void transfer(SimpleQueue<String> src,
                                    SimpleQueue<String> dest)
            throws InterruptedException {
            // Acquire the locks for src and dest.  This causes the
            // sporadic deadlocks when src and dest are reversed.
            synchronized(src) {
                synchronized(dest) {
                    // Remove each element from src and put it into dest.
                    while(!src.isEmpty()) 
                        dest.put(src.take());
                }
            }
        }
    }

    /**
     * Entry point into the program that creates two instances of
     * SimpleQueue (aQueue and bQueue) and two Threads that attempt to
     * transfer the contents of aQueue and bQueue in opposite orders.
     * Although this will sometimes work, it also often deadlocks
     * since the TransferRunnable.transfer() method running in one
     * Thread will acquire aQueue's monitor lock, while the
     * TransferRunnble.transfer() method running in another Thread
     * will acquire bQueue's monitor lock.  At this point, both
     * Threads are waiting to acquire the other SimpleQueue's monitor
     * lock, which causes a circular wait that doesn't terminate!
     */
    @Test(timeout=5_000)
    public void testDeadlockQueue() {
        // Designated the number of iterations to run in each thread.
        int iterations = 1_000_000;
        boolean deadlock = true;

        // Create two SimpleQueue's.
        final SimpleQueue<String> aQueue = new SimpleQueue<>();
        final SimpleQueue<String> bQueue = new SimpleQueue<>();

        // Create/start a Thread that transfers the contents of aQueue
        // to bQueue.
        Thread transfer1 = new Thread
            (new TransferRunnable(aQueue,
                                  bQueue,
                                  iterations));

        // Create/start a Thread that transfers the contents of bQueue
        // to aQueue, which is the reverse of what Thread t1 does (and
        // thus can lead to deadlock).
        Thread transfer2 = new Thread
            (new TransferRunnable(deadlock ? bQueue : aQueue,
                                  deadlock ? aQueue : bQueue,
                                  iterations));
        System.out.println("starting first transfer thread");
        transfer1.start();
        
        System.out.println("starting second transfer thread");
        transfer2.start();

        // Barrier synchronization.
        try {
            transfer1.join();
            System.out.println("joined first transfer thread");
            transfer2.join();
            System.out.println("joined second transfer thread");
        } catch (Exception e) {
            System.out.println("caught exception");
        }
    }
}
