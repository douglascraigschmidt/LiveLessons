package edu.vandy;

/**
 * Defines an interface for a bounded queue.
 */
public interface BoundedQueue<E> {
    /**
     * Inserts the specified element into this queue, waiting if
     * necessary for space to become available.
     *
     * @param e the element to add
     * @throws InterruptedException if interrupted while waiting
     */
    default void put(E e)
        throws InterruptedException {
    } 

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     *
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    default E take() 
        throws InterruptedException {
        return null;
    } 

    /**
     * Retrieves and removes the head of this queue, or returns {@code
     * null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
     default E poll() {
         return null;
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
     default boolean offer(E e) {
         return false;
     }

    /**
     * Returns <tt>true</tt> if this queue contains no elements, else false.
     *
     * @return <tt>true</tt> if this queue contains no elements, else false.
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this queue is full, else false.
     *
     * @return <tt>true</tt> if this queue is full, else false.
     */
    boolean isFull();

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this collection
     */
    int size();
}
