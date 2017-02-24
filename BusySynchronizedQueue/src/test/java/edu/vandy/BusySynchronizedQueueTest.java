package edu.vandy;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Test program for the BusySynchronizedQueue that is inefficient due
 * to its use of "busy waiting".
 */
public class BusySynchronizedQueueTest { 
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
     * This producer runs in a separate Java thread and passes strings
     * to a consumer thread via a shared BoundedBlockingQueue.
     */
    private static class Producer<BQ extends BoundedQueue<Integer>>
                   implements Runnable {
        /**
         * This queue is shared with the consumer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the @a blockingQueue field.
         */
        Producer(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a Java thread and passes strings to a
         * consumer thread via a shared BoundedBlockingQueue.
         */
        public void run() {
            for(int i = 0; i < mMaxIterations; ) {
                // Calls the offer() method.
                if (mQueue.offer(i)) {
                    i++;
                    mCount.incrementAndGet();
                }
            }
        }
    }

    /**
     * This consumer runs in a Java thread and receives strings from a
     * producer thread via a shared BoundedBlockingQueue.
     */
    private static class Consumer<BQ extends BoundedQueue<Integer>>
                   implements Runnable {
        /**
         * This queue is shared with the producer.
         */
        private final BQ mQueue;
        
        /**
         * Constructor initializes the @a blockingQueue data member.
         */
        Consumer(BQ blockingQueue) {
            mQueue = blockingQueue;
        }

        /**
         * This method runs in a Java thread and receives integers
         * from a producer thread via a shared BoundedBlockingQueue.
         */
        public void run() {
            Integer integer = null;
            int nullCount = 0;

            // Get the first item from the queue.
            Integer previous;

            // Get the first non-null value.
            while ((previous = mQueue.poll()) == null)
                continue;

            mCount.decrementAndGet();

            for (int i = 1; i < mMaxIterations; ) {
                // Try to get the next integer.
                integer = mQueue.poll();
                        
                // Only update the state if we get a non-null
                // value from take().
                if (integer != null) {
                    if ((i % (mMaxIterations / 10)) == 0)
                        System.out.println(integer);

                    mCount.decrementAndGet();
                    i++;

                    // Make sure the entries are ordered.
                    assertEquals(previous + 1, integer.intValue());
                    previous = integer;

                } else {
                    nullCount++;
                    // Thread.yield();
                }
            }

            assertEquals(0, mCount.get());

            System.out.println("Final size of the queue is " 
                               + mQueue.size()
                               + "\nmCount is "
                               + mCount.get()
                               + "\nFinal value is "
                               + integer
                               + "\nnumber of null returns from take() is "
                               + nullCount
                               + "\nmCount + nullCount is "
                               + (mCount.get() + nullCount));
        }
    }

    /**
     * Main entry point that tests the BusySynchronizedQueue class.
     */
    @Test
    public void testBusySynchronizedQueue() {
        final BusySynchronizedQueue<Integer> busySynchronizedQueue =
            new BusySynchronizedQueue<>(sQUEUE_SIZE);

        try {
            // Create producer and consumer threads.
            Thread[] threads = new Thread[] {
                new Thread(new Producer<>(busySynchronizedQueue)),
                new Thread(new Consumer<>(busySynchronizedQueue))
            };

            // Record the start time.
            long startTime = System.nanoTime();

            // Start all the threads.
            for (Thread thread : threads)
                thread.start();

            // Wait for all threads to stop.
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
