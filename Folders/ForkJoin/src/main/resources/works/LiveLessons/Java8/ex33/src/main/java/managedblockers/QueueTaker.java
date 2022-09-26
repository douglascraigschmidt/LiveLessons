package managedblockers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;

/**
 * Implements a ManagedBlocker that potentially blocks until an
 * item is available in a {@link BlockingQueue}.
 */
public class QueueTaker<E> 
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
    public QueueTaker(BlockingQueue<E> q) { mQueue = q; }

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

