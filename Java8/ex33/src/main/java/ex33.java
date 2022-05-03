import managedblockers.ManagedLocker;
import managedblockers.QueueTaker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static utils.ExceptionUtils.rethrowRunnable;

/**
 * This example shows how to apply the ForkJoinPool.ManagedBlocker
 * interface, which complete the code fragments shown in the Java
 * documentation at
 * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html
 */
public class ex33 {
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

