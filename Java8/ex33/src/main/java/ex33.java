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
 * This example shows how to apply the ForkJoinPool.ManagedBlocker
 * interface, which complete the code fragments shown in the Java
 * documentation at
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
        final Lock mLock;

        /**
         * Keeps track of whether the lock was obtained.
         */
        boolean mHasLock = false;

        /**
         * Constructor initializes the field.
         */
        ManagedLocker(Lock lock) {
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
         // A ReentrantLock used for the tests.
         Lock lock = new ReentrantLock();

        // Create a ManagedLocker that's associated with sLock.
        ManagedLocker managedLocker = new ManagedLocker(lock);

        // Acquire the lock.
        lock.lock();

        System.out.println("Waiting to acquire the lock at time "
                           + System.currentTimeMillis() / 1000);

        ForkJoinPool
            // Use the common fork-join pool.
            .commonPool()

            // Initiate a managedBlock() operation that will block
            // until the lock is available.
            .execute(() -> {
                     rethrowRunnable(() -> 
                                     ForkJoinPool.managedBlock(managedLocker));

                     System.out.println("Actually acquired the lock at time "
                                        + System.currentTimeMillis() / 1000);
                });
                         

        // Sleep for one second.
        Thread.sleep(1000);

        // Release the lock.
        lock.unlock();
    }

    /**
     * Test the QueueTaker implementation.
     */
    private static void testQueueTaker() throws InterruptedException {
        // A BlockingQueue used for the tests that's implemented
        // via an ArrayBlockingQueue.
        BlockingQueue<String> queue =
                new ArrayBlockingQueue<>(10);

        // Create a QueueTaker that's associated with sQueue.
        QueueTaker<String> queueTaker = new QueueTaker<>(queue);

        System.out.println("Waiting to take an item at time "
                           + System.currentTimeMillis() / 1000);

        ForkJoinPool
            // Use the common fork-join pool.
            .commonPool()

            // Initiate a managedBlock() operation that will block
            // until there's an item in sQueue.
            .execute(() -> {
                     rethrowRunnable(() ->
                                     ForkJoinPool.managedBlock(queueTaker));
                     
                     // Get the item obtained by the queueTaker.
                     String s = queueTaker.getItem();

                     System.out.println("Actually took an item at time "
                                        + System.currentTimeMillis() / 1000);

                     // Print the item obtained by the queueTaker.
                     System.out.println("Took item " + s);
                });

        // Sleep for one second.
        Thread.sleep(1000);

        // Put an item into the queue.
        queue.put("hello");
    }
}

