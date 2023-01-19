package edu.vandy.simpleblockingboundedqueue.model;

import java.util.LinkedList;

/*
 * Defines an implementation of the SimpleBlockingQueue interface that works
 * properly when accessed via multiple threads since it's synchronized
 * properly.
 */
public class SemaphoresBlockingBoundedQueue<E>
      implements BoundedQueue<E> {
    /**
     * The queue consists of an array of E's
     */
    private final E[] mItems;

    /**
     *
     */
    private final Semaphore mAvailableItems;

    /**
     *
     */
    private final Semaphore mAvailableSpaces;

    /**
     *
     */
    private int mPutPosition;

    /**
     *
     */
    private int mTakePosition;

    /**
     * Create a queue with the given capacity.
     */
    public SemaphoresBlockingBoundedQueue(int capacity) {
        if (capacity <= 0) 
            throw new IllegalArgumentException();
        mAvailableItems = new Semaphore(0);
        mAvailableSpaces = new Semaphore(capacity);
        mItems = (E[]) new Object[capacity];
    }

    /**
     * Inserts the specified element into this queue, waiting if
     * necessary for space to become available.
     *
     * @param e the element to add
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public void put(E e)
        throws InterruptedException {
        mAvailableSpaces.acquire();
        doInsert(e);
        mAvailableItems.release();
    } 

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     *
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public E take() throws InterruptedException {
        mAvailableItems.acquire();
        final E item = doExtract();
        mAvailableSpaces.release();
        return item;
    } 

    /**
     * Returns true if the queue is empty, else false.
     */
    @Override
    public boolean isEmpty() {
        return mAvailableItems.availablePermits() == 0;
    }

    /**
     * Returns true if the queue is full, else false.
     */
    @Override
    public boolean isFull() {
        return mAvailableSpaces.availablePermits() == 0;
    }

    /**
     *
     */
    private synchronized void doInsert(E x) {
        int i = mPutPosition;
        items[i] = x;
        mPutPosition = ++i == mItems.length ? 0 : i;
    }

    /**
     *
     */
    private synchronized E doExtract() {
        int i = mTakePosition;
        final E x = mItems[i];
        mItems[i] = null;
        mTakePosition = ++i == mItems.length ? 0 : i;
        return x;
    }

    /**
     * Returns the number of elements in this queue.
     */
    @Override
    public int size() {
        // @@
        return 0;
    }
}
