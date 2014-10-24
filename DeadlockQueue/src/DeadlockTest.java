/**
 * @class DeadlockTest
 *
 * @brief Illustrates how deadlock can occur due to "circular
 *        waiting".  Moreover, deadlocks occur sporatically, which
 *        makes it even harder to identify and diagnose the problem!
 */
class DeadlockTest {
    /**
     * @class TransferRunnable
     * 
     * @brief Helper class that's passed a parameter to a Thread.
     */
    static class TransferRunnable implements Runnable {
        /**
         * Transfer contents of src to dest in a synchronized manner since
         * SimpleQueue lacks have internal synchronization.
         */
        public static void transfer(SimpleQueue<String> src,
                                    SimpleQueue<String> dest)
            throws InterruptedException {
            // Acquire the locks for src and dest.
            synchronized(src) {
                synchronized(dest) {
                    // Remove each element from src and put it into dest.
                    while(!src.isEmpty()) 
                        dest.put(src.take());
                }
            }
        }

        /**
         * First instance of the SimpleQueue.
         */
        private SimpleQueue<String> mAQueue;

        /**
         * Second instance of the SimpleQueue.
         */
        private SimpleQueue<String> mBQueue;

        /**
         * Number of iterations to run the test.
         */
        private int mIterations;

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
        public void run() {
            try {
                for (int i = 0; i < mIterations; ++i)
                    TransferRunnable.transfer(mAQueue,
                                              mBQueue);
            } catch (Exception e) {
                System.out.println("caught exception");
            }
        }
    }

    /**
     * Entry point into the program that creates two instances of
     * SimpleQueue (aQueue and bQueue) and two Threads that attempt to
     * transfer the contents of aQueue and bQueue in opposite orders.
     * Although this will work sometimes, it also often deadlocks
     * since the Deadlock.transfer() method running in one Thread will
     * acquire aQueue's monitor lock, while the Deadlock.transfer()
     * method running in another Thread will acquire bQueue's monitor
     * lock.  At this point, both Threads are waiting to acquire the
     * other SimpleQueue's monitor lock, which causes a circular wait
     * that doesn't terminate!
     */
    static public void main(String[] args) {
        // Designated the number of iterations to run in each thread.
        int iterations =
            args.length > 0 ? Integer.parseInt(args[0]) : 1000000;

        // Create two SimpleQueue's.
        final SimpleQueue<String> aQueue = new SimpleQueue<String>();
        final SimpleQueue<String> bQueue = new SimpleQueue<String>();

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
            (new TransferRunnable(bQueue,
                                  aQueue,
                                  iterations));
        System.out.println("starting first transfer thread");
        transfer1.start();
        
        System.out.println("starting second transfer thread");
        transfer2.start();

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
