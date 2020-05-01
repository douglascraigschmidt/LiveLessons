package utils;

import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

/**
 * Implements a wrapper that adapts the ConcurrentHashMap
 * implementation to provide a simple ConcurrentHashSet ADT, which is
 * sadly lacking from Java.
 */
public class StampedLockHashMap<K, V>
       extends AbstractMap<K, V> 
       implements Map<K, V> {
    /**
     * The HashMap that's used to implement the StampedLockHashMap.
     */
    private final HashMap<K, V> mMap;

    /**
     * The StampedLock instance used to protect the HashMap.
     */
    private final StampedLock mStampedLock;

    /**
     * Constructor initializes the field.
     */
    public StampedLockHashMap(){
        mMap = new HashMap<>();
        mStampedLock = new StampedLock();
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    @Override
    public int size() {
        return mMap.size();
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
     * Removes all of the elements from this collection.  The
     * collection will be empty after this method returns.
     */
    @Override
    public void clear(){
        mMap.clear();
    }

    /**
     * Return the entrySet.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return mMap.entrySet();
    }

    /**
     * If the specified key is not already associated with a value (or
     * is mapped to null), attempts to compute its value using the
     * given mapping function and enters it into this map unless null.
     */
    public V computeIfAbsent(K key,
                             Function<? super K,? extends V> mappingFunction) {
        switch (Options.instance().stampedLockStrategy()) {
        case 'W': return computeIfAbsentWriteLock(key, mappingFunction);
        case 'C': return computeIfAbsentConditionalWrite(key, mappingFunction);
        case 'O': return computeIfAbsentOptimisticRead(key, mappingFunction);
        default: throw new IllegalArgumentException();
        }
    }

    /**
     * This implementation uses a conventional write lock.
     */
    private V computeIfAbsentWriteLock(K key, Function<? super K,? extends V> mappingFunction) {
        // Acquire the lock for writing.
        long stamp = mStampedLock.writeLock();

        try {
            // Get the current value (if any) with the read lock held.
            V value = mMap.get(key);

            // If value is null then this is the first time in.
            if (value == null) {
                // Apply the mapping function to compute the value.
                value = mappingFunction.apply(key);

                // If mapping function worked then add value to map.
                if (value != null) {
                            // Put the key in the map on success.
                    mMap.put(key, value);
                }
            }

            // Return the value (either new or old).
            return value;
        } finally {
            // Unlock the stamp (which might be read or write).
            mStampedLock.unlock(stamp);
        }
    }

    /**
     * This implementation uses a conditional write lock.
     */
    private V computeIfAbsentConditionalWrite(K key,
                                              Function<? super K,? extends V> mappingFunction) {
        // Acquire the lock for reading.
        long stamp = mStampedLock.readLock();

        try {
            // Get the current value (if any) with the read lock held.
            V value = mMap.get(key);

            // If value is null then this is the first time in.
            if (value == null) {
                // Apply the mapping function to compute the value.
                value = mappingFunction.apply(key);

                // If mapping function worked then add value to map.
                if (value != null) {
                    for (;;) {
                        mStampedLock.unlockRead(stamp);
                        // Try converting to writelock (non-blocking).
                        long ws = mStampedLock .writeLock();
                        // .tryConvertToWriteLock(stamp);

                        if (ws != 0L) {
                            // Update stamp to ws.
                            stamp = ws;

                            // Put the key in the map on success.
                            mMap.put(key, value);

                            // Break out of the loop.
                            break;
                        } else {
                            // Release the read lock.
                            mStampedLock.unlockRead(stamp);

                            // Block acquiring the write lock.
                            stamp = mStampedLock.writeLock();
                        }
                    }
                }
            }

            // Return the value (either new or old).
            return value;
        } finally {
            // Unlock the stamp (which might be read or write).
            mStampedLock.unlock(stamp);
        }
    }

    /**
     * This implementation uses a optimistic read lock.
     */
    private V computeIfAbsentOptimisticRead(K key,
                                            Function<? super K,? extends V> mappingFunction) {
        // Acquire the lock for optimistic reading.
        long stamp = mStampedLock.tryOptimisticRead();

        // Get the current value (if any) with the read lock held.
        V value = mMap.get(key);

        if (!mStampedLock.validate(stamp)) {
            return computeIfAbsentConditionalWrite(key,
                                                   mappingFunction);
        } else {
            stamp = mStampedLock.tryOptimisticRead();

            // If value is null this is the first time in.
            if (value == null) {
                // Apply mapping function to compute the value.
                value = mappingFunction.apply(key);

                if (!mStampedLock.validate(stamp)) {
                    return computeIfAbsentConditionalWrite(key,
                                                           mappingFunction);
                }

                if (value != null) {
                    // Mapping function worked so add value to
                    // map.

                    for (;;) {
                        stamp = mStampedLock.tryConvertToWriteLock(stamp);
                        if (stamp != 0L) {
                            // Put the key in the map on success.
                            mMap.put(key, value);

                            // Unlock the write lock.
                            mStampedLock.unlockWrite(stamp);

                            // Break out of the loop.
                            break;
                        } else {
                            // Block acquiring the write lock.
                            stamp = mStampedLock.writeLock();
                        }
                    }
                }
            }
        }

        // Return the value (either new or old).
        return value;
    }
}
