package edu.vandy.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implements a simple set.
 */
public class SimpleSet<E>
       extends AbstractSet<E> {
    /**
     * The array buffer that stores all the set elements.  The
     * capacity is the length of this array buffer.
     */
    private Object[] mElementData;

    /**
     * The size of the set (the number of elements it contains).
     */
    private int mSize;

    /**
     * Index to the last element in the array.
     */
    private int mEnd;

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /*
     * The following methods and nested class use Java 7 features.
     */

    /**
     * Constructs an empty set with an initial capacity of ten.
     */
    public SimpleSet() {
        // Preallocate DEFAULT_CAPACITY (i.e., 10) elements.
        mElementData = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set.
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Adds the specified element to this set.
     *
     * @param element element to be added
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean add(E element){
        if (contains(element))
            return false;

        // Check that there's sufficient capacity in the array,
        // expanding if it needed.
        checkCapacityAndExpandIfNecessary(mSize + 1);  

        // Add the element at the rear of the array.
        mElementData[mEnd] = element;

        // Update the index that keeps track of the end of the array.
        mEnd++;

        // Increment the size of the array.
        mSize++;
        return true;
    }

    /**
     * Ensure the array is large enough to hold @a minCapacity
     * elements.  The array will be expanded if necessary.
     */
    private void checkCapacityAndExpandIfNecessary(int minCapacity) {
        if (minCapacity - mElementData.length > 0) {
            int oldCapacity = mElementData.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;

            Object[] newElementData =
                new Object[newCapacity];

            System.arraycopy(mElementData,
                             0,
                             newElementData, 0,
                             mSize);

            mEnd = mSize;
            mElementData = newElementData;
        }
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
        for (int i = 0; i < mEnd; i++)
            if (o.equals(mElementData[i]))
                return true;

        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new SimpleSetIterator();
    }

    /**
     * This class defines an iterator over the elements in a SimpleSet
     * in proper sequence.
     */
    private class SimpleSetIterator 
           implements Iterator<E> {
        /**
         * Current position in the array (defaults to 0).
         */
        // TODO - you fill in here.
        private int mPos;

        /**
         * Index of last element returned; -1 if no such element.
         */
        // TODO - you fill in here.
        private int mLastRet = -1; 

        /** 
         * @return True if the iteration has more elements that
         * haven't been iterated through yet, else false.
         */
        @Override
        public boolean hasNext() {
        // TODO - you fill in here (replace false with proper boolean
        // expression).
            return mPos < mSize;
        }

        /**
         * @return The next element in the iteration.
         */
        @Override
        public E next() {
            // TODO - you fill in here (replace null with proper
            // return value).
            //noinspection unchecked
            return (E) mElementData[mLastRet = mPos++];
        }
    }
}
