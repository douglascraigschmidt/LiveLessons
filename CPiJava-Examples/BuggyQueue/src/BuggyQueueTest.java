import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/**
 * @class BuggyQueueTest
 * 
 * @brief Test program for the SimpleQueue that induces race
 *        conditions due to lack of synchronization.
 */
public class BuggyQueueTest
{ 
    /**
     * Maximum number of iterations.
     */
    private final static int mMaxIterations = 1000000;

    /**
     * Maximum size of the queue.
     */
    private final static int mQueueSize = 10;

    /**
     * Count the number of iterations.
     */
    private final static AtomicInteger mCount =
        new AtomicInteger(0);

    /**
     * @class ProducerThread
     *
     * @brief This producer runs in a separate Java Thread and passes
     *        Strings to a consumer Thread via a shared BlockingQueue.
     */
    static class ProducerThread<BQ extends BlockingQueue> extends Thread {
        /**
         * This queue is shared with the consumer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the BlockingQueue data
         * member.
         */
        ProducerThread(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a separate Java Thread and passes
         * Strings to a consumer Thread via a shared BlockingQueue.
         */
        public void run(){ 
            try {
                for(int i = 0; i < mMaxIterations; i++) {
                    mCount.incrementAndGet();

                    // Calls the put() method.
                    mQueue.put(Integer.toString(i));
                }
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
    }

    /**
     * @class ConsumerThread
     *
     * @brief This consumer runs in a separate Java Thread and
     *        receives Strings from a producer Thread via a shared
     *        BlockingQueue.
     */
    static class ConsumerThread<BQ extends BlockingQueue> extends Thread {
        /**
         * This queue is shared with the producer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the BlockingQueue data member.
         */
        ConsumerThread(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a separate Java Thread and receives
         * Strings from a producer Thread via a shared BlockingQueue.
         */
        public void run(){ 
            Object s = null;
            try {
                for(int i = 0; i < mMaxIterations; i++) {
                    // Calls the take() method.
                    s = mQueue.take();
                    
                    mCount.decrementAndGet();
                    
                    if((i % (mMaxIterations / 10)) == 0)
                        System.out.println(s == null ? "<null>" : s);
                }
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
            System.out.println("Final size of the queue is " 
                               + mQueue.size()
                               + "\nmCount is "
                               + mCount.get()
                               + "\nFinal value is "
                               + s);
        }
    }

    /**
     * Main entry point that tests the SimpleQueue class.
     */
    public static void main(String argv[]) {
        final SimpleQueue<String> simpleQueue =
            new SimpleQueue<String>(); // (mQueueSize);

        try {
            // Create a ProducerThread.
            Thread producer =
                new ProducerThread(simpleQueue);
        
            // Create a ConsumerThread.
            Thread consumer =
                new ConsumerThread(simpleQueue);

            // Run both Threads concurrently.
            producer.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            consumer.start();

            // Wait for both Threads to stop.
            producer.join();
            consumer.join();
        } catch (Exception e) {
            System.out.println("caught exception");
        }
    }
}
