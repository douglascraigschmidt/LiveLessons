package utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements a wrapper that adapts the ConcurrentTreeMap
 * implementation to provide a simple ConcurrentHashSet ADT, which is
 * sadly lacking from Java.
 */
public class ConcurrentHashSet<E>
       extends AbstractSet<E> 
       implements Set<E> {
    /**
     * The ConcurrentMap that's used to implement the
     * ConcurrentHashSet.
     */
    private final ConcurrentMap<E, Object> mMap;

    /**
     * A dummy value object needed by ConcurrentTreeMap.
     */
    private static final Object mDummyValue = new Object();

    /**
     * Constructor initializes the field.
     */
    ConcurrentHashSet(){
        mMap = new ConcurrentHashMap<E, Object>();
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set.
     */
    @Override
    public int size() {
        return mMap.size();
    }

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    @Override
    public Iterator<E> iterator() {
        return mMap.keySet().iterator();
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * Adds the specified element to this set.
     *
     * @param e element to be added
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean add(E e){
        return mMap.put(e, ConcurrentHashSet.mDummyValue) == null;
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified
     *         element
     */
    @Override
    public boolean contains(Object o){
        return mMap.containsKey(o);
    }

    /**
     * Removes all of the elements from this collection.  The
     * collection will be empty after this method returns.
     */
    @Override
    public void clear(){
        mMap.clear();
    }

    /**
     * Removes the specified element from this set if present.
     *
     * @param  e Element that is being removed from the set
     * @return <tt>true</tt> if an element was removed as a result of this call
     */
    @Override
    public boolean remove(Object e){
        return mMap.remove(e) == ConcurrentHashSet.mDummyValue;
    }

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * This is equivalent to
     *  <pre> {@code
     * if (!map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return map.get(key);
     * }</pre>
     *
     * except that the action is performed atomically.
     *
     * @return {@code true} if the element was added to the set,
     *         {@code false} if element was already in the set.
     */
    public boolean putIfAbsent(E e) {
        return mMap.putIfAbsent(e,
                                ConcurrentHashSet.mDummyValue) == null;
    }
}
