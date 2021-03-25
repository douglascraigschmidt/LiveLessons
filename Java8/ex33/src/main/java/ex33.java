import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowRunnable;

/**
 * This example shows examples of applying the
 * ForkJoinPool.ManagedBlocker interface, which are based on the code
 * fragments shown in the Java documentation at
 * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html
 */
public class ex33 {
    /**
     * Implements a ManagedBlocker that potentially blocks until a
     * {@link ReentrantLock} is available.
     */
    static class ManagedLocker
           implements ForkJoinPool.ManagedBlocker {
        /**
         * The {@link ReentrantLock}.
         */ 
        final ReentrantLock mLock;

        /**
         * Keeps track of whether the lock was obtained.
         */
        boolean mHasLock = false;

        /**
         * Constructor initializes the field.
         */
        ManagedLocker(ReentrantLock lock) { 
            mLock = lock; 
        }

        /**
         * Tries to obtain the lock if it's immediately available, but
         * doesn't block if it's not available.
         */
        public boolean isReleasable() {
            return mHasLock || (mHasLock = mLock.tryLock()); 
        }

        /**
         * Block until the lock is available and lock it.
         */
        public boolean block() {
            if (!mHasLock) 
                mLock.lock();
            return true;  
        } 
    }

    /**
     * Implements a ManagedBlocker that potentially blocks until an
     * item is available in a {@link BlockingQueue}.
     */
    static class QueueTaker<E> 
           implements ForkJoinPool.ManagedBlocker {
        /**
         * The {@link BlockingQueue}.
         */ 
        final BlockingQueue<E> mQueue;

        /**
         * The item obtained from the {@link BlockingQueue}.
         */
        volatile E mItem = null;

        /**
         * Constructor initializes the field.
         */
        QueueTaker(BlockingQueue<E> q) { mQueue = q; }

        /**
         * Tries to obtain an item from the queue if it's immediately
         * available, but doesn't block if it's not available.
         */
        public boolean isReleasable() { 
            return mItem != null || (mItem = mQueue.poll()) != null; 
        }

        /**
         * Block until the item is available on the queue.
         */
        public boolean block() throws InterruptedException {
            if (mItem == null)
                mItem = mQueue.take();
            return true; 
        }

        /**
         * @return Return the item on the queue
         */
        public E getItem() { 
            return mItem; 
        }
    }

    /**
     * A {@link ReentrantLock} used for the tests.
     */
    static ReentrantLock mLock = new ReentrantLock();

    /**
     * A {@link BlockingQueue} used for the tests that's implemented
     * via an {@link ArrayBlockingQueue}.
     */
    static BlockingQueue<String> mQueue = new ArrayBlockingQueue<>(10);

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) throws InterruptedException {
        // Test the ManagedLocker implementation.
        testManagedLocker();

        // Test the QueueTaker implementation.
        testQueueTaker();
    }

    /**
     * Test the ManagedLocker implementation.
     */
    private static void testManagedLocker() throws InterruptedException {
        // Create a ManagedLocker that's associated with mLock.
        ManagedLocker managedLocker = new ManagedLocker(mLock);

        // Acquire the lock.
        mLock.lock();

        System.out.println("Waiting to acquire the lock at time "
                           + System.currentTimeMillis() / 1000);

        ForkJoinPool
            // Use the common fork-join pool.
            .commonPool()

            // Initiate a managedBlock() operation that will block until
            // the lock is available.
            .execute(() ->
                     rethrowRunnable(() ->
                                     ForkJoinPool.managedBlock(managedLocker)));

        // Sleep for one second.
        Thread.sleep(1000);

        System.out.println("Actually acquired the lock at time "
                + System.currentTimeMillis() / 1000);
    }

    /**
     * Test the QueueTaker implementation.
     */
    private static void testQueueTaker() throws InterruptedException {
        // Create a QueueTaker that's associated with mQueue.
        QueueTaker<String> queueTaker = new QueueTaker<>(mQueue);

        System.out.println("Waiting to take an item at time "
                + System.currentTimeMillis() / 1000);

        ForkJoinPool
                // Use the common fork-join pool.
                .commonPool()

                // Initiate a managedBlock() operation that will block until
                // there's an item in mQueue.
                .execute(() ->
                         rethrowRunnable(() ->
                                         ForkJoinPool.managedBlock(queueTaker)));

        // Sleep for one second.
        Thread.sleep(1000);

        // Put an item into the queue.
        mQueue.put("hello");

        System.out.println("Actually took an item at time "
                + System.currentTimeMillis() / 1000);

        // Get the item obtained by the queueTaker and print it.
        System.out.println("Took item " + queueTaker.getItem());
    }
}

