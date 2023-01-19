package edu.vandy.simpleblockingboundedqueue;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import edu.vandy.simpleblockingboundedqueue.model.BoundedQueue;
import edu.vandy.simpleblockingboundedqueue.model.SimpleBlockingBoundedQueue;

/**
 * Test program for the SimpleBoundedBlockingQueue that fixes race conditions
 * by having proper synchronization (i.e., mutual exclusion and
 * coordination).
 */
public class SimpleBlockingBoundedQueueTest {
    /**
     * Maximum number of iterations.
     */
    private final static int mMaxIterations = 100000;

    /**
     * Maximum size of the queue.
     */
    private final static int sQUEUE_SIZE = 10;

    /**
     * Count the number of iterations.
     */
    private final static AtomicInteger mCount =
        new AtomicInteger(0);

    /**
     * This producer runs in a separate Java thread and passes integers
     * to a consumer thread via a shared SimpleBlockingQueue.
     */
    private static class Producer<BQ extends BoundedQueue<Integer>>
           implements Runnable {
        /**
         * This queue is shared with the consumer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the SimpleBlockingQueue data
         * member.
         */
        Producer(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a separate Java thread and passes
         * integers to a consumer thread via a shared SimpleBlockingQueue.
         */
        public void run(){ 
            try {
                for (int i = 0; i < mMaxIterations; i++) {
                    mCount.incrementAndGet();

                    // Call the put() method.
                    mQueue.put(i);
                }
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
    }

    /**
     * This consumer runs in a separate Java thread and receives
     * integers from a producer thread via a shared SimpleBlockingQueue.
     */
    private static class Consumer<BQ extends BoundedQueue<Integer>>
           implements Runnable {
        /**
         * This queue is shared with the producer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the SimpleBlockingQueue data member.
         */
        Consumer(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a separate Java thread and receives
         * integers from a producer thread[q via a shared SimpleBlockingQueue.
         */
        public void run() {
            Integer integer = null;

            try {
                // Get the first item from the queue.
                Integer previous = mQueue.take();
                mCount.decrementAndGet();

                for (int i = 1; i < mMaxIterations; ++i) {
                    // Calls the take() method.
                    integer = mQueue.take();

                    // Make sure the entries are ordered.
                    assertEquals(previous + 1, integer.intValue());
                    previous = integer;
                        
                    if ((i % (mMaxIterations / 10)) == 0)
                        System.out.println(integer);

                    mCount.decrementAndGet();
                }
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
            assertEquals(0, mCount.get());

            System.out.println("Final size of the queue is " 
                               + mQueue.size()
                               + "\nmCount is "
                               + mCount.get()
                               + "\nFinal value is "
                               + integer);
        }
    }

    /**
     * Main entry point that tests the SimpleBoundedQueue class.
     */
    @Test
    public void testSimpleBlockingBoundedQueue() {
        final SimpleBlockingBoundedQueue<Integer> simpleQueue =
            new SimpleBlockingBoundedQueue<>(sQUEUE_SIZE);

        try {
            // Create producer and consumer threads.
            Thread[] threads = new Thread[] {
                new Thread(new Producer<>(simpleQueue)),
                new Thread(new Consumer<>(simpleQueue))
            };

            // Record the start time.
            long startTime = System.nanoTime();

            // Start both threads.
            for (Thread thread : threads)
                thread.start();

            // Wait for both threads to stop.
            for (Thread thread : threads)
                thread.join();

            System.out.println("test ran in "
                               + (System.nanoTime() - startTime) / 1_000_000
                               + " msecs");
        } catch (Exception e) {
            System.out.println("caught exception");
        }
    }
}
