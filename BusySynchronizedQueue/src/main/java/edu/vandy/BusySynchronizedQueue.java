package edu.vandy;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/*
 * Implements the BoundedQueue interface that works properly when
 * accessed via multiple threads since it's synchronized properly, but
 * is inefficient since due to its "busy waiting".
 */
class BusySynchronizedQueue<E> 
      implements BoundedQueue<E> {
    /**
     * The queue consists of a LinkedList of E's.
     */
    private LinkedList<E> mList = new LinkedList<>();

    /**
     * The maximum capacity of the queue or Integer.MAX_VALUE if none.
     */
    private final int mCapacity;

    /**
     * Create a SimpleBlocking queue with a capacity of
     * Integer.MAX_VALUE.
     */
    public BusySynchronizedQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create a SimpleBlocking queue with the given capacity.
     */
    public BusySynchronizedQueue(int capacity) {
        if (capacity <= 0) 
            throw new IllegalArgumentException();
        mCapacity = capacity;
        mList = new LinkedList<>();
    }

    /**
     * Returns <tt>true</tt> if this queue contains no elements, else false.
     *
     * @return <tt>true</tt> if this queue contains no elements, else false.
     */
    @Override
    public synchronized boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Returns <tt>true</tt> if this queue is full, else false.
     *
     * @return <tt>true</tt> if this queue is full, else false.
     */
    @Override
    public synchronized boolean isFull() {
        return mList.size() == mCapacity;
    }

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this collection
     */
    @Override
    public synchronized int size() {
        return mList.size();
    }

    /**
     * Retrieves and removes the head of this queue, or returns {@code
     * null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    @Override
    public synchronized E poll() {
       return mList.poll();
    }

    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions, returning
     * {@code true} upon success and {@code false} if no space is currently
     * available.
     *
     * @return {@code true} if the element was added to this queue, else
     *         {@code false}
     */
    @Override
    public synchronized boolean offer(E e) {
        if (!isFull()) {
            mList.add(e);
            return true;
        } else
            return false;
    }
}






