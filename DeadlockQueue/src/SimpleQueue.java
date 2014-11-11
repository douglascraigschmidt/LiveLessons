import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * @class SimpleQueue
 *
 * @brief Defines an implementation of the BlockingQueue interface
 *        that (intentially) doesn't work properly when accessed via
 *        multiple threads since it's not synchronized properly.
 */
class SimpleQueue<E> implements BlockingQueue<E> {
    /**
     * The queue consists of a List of E's.
     */
    private List<E> mList = new ArrayList<E>();

    /**
     * True if the queue is empty.
     */
    public boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Add a new E to the end of the queue.
     */
    public void put(E msg) throws InterruptedException {
        mList.add(msg);
    } 

    /**
     * Remove the E at the front of the queue.
     */
    public E take() throws InterruptedException {
        return mList.remove(0);
    } 

    /**
     * Returns the number of elements in this queue.
     */
    public int size() {
        return mList.size();
    }

    /**
     * All these methods are inherited from the BlockingQueue
     * interface. They are defined as no-ops to ensure the "Buggyness"
     * of this class ;-)
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






