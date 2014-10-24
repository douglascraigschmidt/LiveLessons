import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * @class SimpleBlockingQueue
 *
 * @brief Defines an implementation of the BlockingQueue interface
 *        that works properly when accessed via multiple threads since
 *        it's synchronized properly.
 */
class SimpleBlockingQueue<E> implements BlockingQueue<E> {
    /**
     * The queue consists of a List of E's.
     */
    final private List<E> mList;

    /**
     * The maximum capacity of the queue or Integer.MAX_VALUE if none.
     */
    private final int mCapacity;

    /**
     * Create a SimpleBlocking queue with a capacity of
     * Integer.MAX_VALUE.
     */
    public SimpleBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create a SimpleBlocking queue with the given capacity.
     */
    public SimpleBlockingQueue(int capacity) {
        if (capacity <= 0) 
            throw new IllegalArgumentException();
        mCapacity = capacity;
        mList = new ArrayList<E>();
    }

    /**
     * Add a new E to the end of the queue, blocking if necessary for
     * space to become available.
     */
    public void put(E e) throws InterruptedException {
        synchronized(this) {
            if (e == null)
                throw new NullPointerException();

            // Wait until the queue is not full.
            while (isFull()) {
                // System.out.println("BLOCKING ON PUT()");
                wait();
            }

            // Add e to the ArrayList.
            mList.add(e);
            
            // Notify that the queue may have changed state, e.g., "no
            // longer empty".
            notifyAll();
        }
    } 

    /**
     * Remove the E at the front of the queue, blocking until there's
     * something in the queue.
     */
    public E take() throws InterruptedException {
        synchronized(this) {
            // Wait until the queue is not empty.
            while (mList.isEmpty()) {
                // System.out.println("BLOCKING ON TAKE()");
                wait();
            }

            final E e = mList.remove(0);
        
            // Notify that the queue may have changed state, e.g., "no
            // longer full".
            notifyAll();
            return e;
        }
    } 

    /**
     * Returns the number of elements in this queue.
     */
    public int size() {
        synchronized(this) {
            return mList.size();
        }
    }

    /**
     * Returns true if the queue is empty, else false.
     */
    public boolean isEmpty() {
        synchronized(this) {
            return mList.size() == 0;
        }
    }

    /**
     * Returns true if the queue is full, else false.  Since this
     * isn't a public method it assumes the monitor lock is held.
     */
    private boolean isFull() {
        return mList.size() == mCapacity;
    }

    /**
     * All these methods are inherited from the BlockingQueue
     * interface. They are defined as no-ops and their implementations
     * are left as an exercise to the reader.
     */
    public int drainTo(Collection<? super E> c) {
        return 0;
    }
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
    public boolean contains(Object o) {
        return false;
    }
    public boolean remove(Object o) {
        return false;
    }
    public int remainingCapacity() {
        return 0;
    }
    public E poll() {
        return null;
    }
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return take();
    }
    public E peek() {
        return null;
    }
    public boolean offer(E e) {
        return false;
    }
    public boolean offer(E e, long timeout, TimeUnit unit) {
        try {
            put(e);
        }
        catch (InterruptedException ex) {
            // Just swallow this exception for this simple (buggy) test.
        }
        return true;
    }
    public boolean add(E e) {
        return false;
    }
    public E element() {
        return null;
    }
    public E remove() {
        return null;
    }
    public void clear() {
    }
    public boolean retainAll(Collection<?> collection) {
        return false;
    }
    public boolean removeAll(Collection<?> collection) {
        return false;
    }
    public boolean addAll(Collection<? extends E> collection) {
        return false;
    }
    public boolean containsAll(Collection<?> collection) {
        return false;
    }
    public Object[] toArray() {
        return null;
    }
    public <T> T[] toArray(T[] array) {
        return null;
    }
    public Iterator<E> iterator() {
        return null;
    }
}
