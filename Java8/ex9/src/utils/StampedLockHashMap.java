package utils;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

/**
 * A concurrent HashMap collection that's implemented using advanced
 * features of StampedLock to protect mutable shared state.
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
     * If {@code key} is not already associated with a value (or is
     * mapped to null) then compute its value using the given {@code
     * mappingFunction} and enter it into the map (unless it's null).
     */
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        // Determine the appropriate strategy to use!
        switch (Options.instance().stampedLockStrategy()) {
        case 'W': return computeIfAbsentWriteLock(key, mappingFunction);
        case 'C': return computeIfAbsentConditionalWrite(key, mappingFunction);
        case 'O': return computeIfAbsentOptimisticRead(key, mappingFunction);
        default: throw new IllegalArgumentException();
        }
    }

    /**
     * This implementation uses a conventional write lock, which is a
     * pessimistic lock.
     */
    private V computeIfAbsentWriteLock(K key,
                                       Function<? super K, ? extends V> mappingFunction) {
        // Acquire the lock for writing.
        long stamp = mStampedLock.writeLock();

        try {
            // Try to get the value from the map via key or apply
            // mappingFunction if value's not yet computed.
            V value = getOrComputeValueUnlocked(key,
                                                mappingFunction);

            // If mapping function worked then add value to map.
            if (value != null) {
                // Put the key in the map on success.
                mMap.put(key, value);
            }

            // Return the value (either new or old).
            return value;
        } finally {
            // Unlock the write stamp.
            mStampedLock.unlockWrite(stamp);
        }
    }

    /**
     * This implementation uses a conditional write lock, which is a
     * bit more optimistic.
     */
    private V computeIfAbsentConditionalWrite(K key,
                                              Function<? super K, ? extends V> mappingFunction) {
        // Acquire the lock for reading.
        long stamp = mStampedLock.readLock();

        try {
            // Try to get the value from the map via key or apply
            // mappingFunction if value's not yet computed.
            V value = getOrComputeValueUnlocked(key,
                                                mappingFunction);

            // If mapping function worked then add value to map.
            if (value != null) {
                for (;;) {
                    // Try converting to writelock (non-blocking).
                    long ws = mStampedLock 
                        .tryConvertToWriteLock(stamp);

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

                        // Start over again with the write lock held.
                        value = getOrComputeValueUnlocked(key,
                                                          mappingFunction);

                        if (value == null)
                            // Something went wrong, so bail out.
                            break;
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
     * This implementation uses a optimistic read lock, which is
     * optimistic by its very nature ;-).
     */
    private V computeIfAbsentOptimisticRead(K key,
                                            Function<? super K, ? extends V> mappingFunction) {
        // "Acquire" the lock for optimistic reading.
        long stamp = mStampedLock.tryOptimisticRead();

        // Get the current value (if any) with the optimistic read
        // lock "acquired".
        V value = mMap.get(key);

        // Check if a writer occurred during this window.
        if (!mStampedLock.validate(stamp)) {
            // Revert to conditional write strategy.
            return computeIfAbsentConditionalWrite(key,
                                                   mappingFunction);
        } else {
            // "Acquire" the lock for optimistic reading.
            stamp = mStampedLock.tryOptimisticRead();

            // If value is null this is the first time in.
            if (value == null) {
                // Apply mapping function to compute the value.
                value = mappingFunction.apply(key);

                // Check if a writer occurred during this window.
                if (!mStampedLock.validate(stamp)) {
                    // Revert to conditional write strategy.
                    return computeIfAbsentConditionalWrite(key,
                                                           mappingFunction);
                }

                if (value != null) {
                    // Mapping function worked so add value to map.

                    // Try converting to writelock (non-blocking).
                    stamp = mStampedLock.tryConvertToWriteLock(stamp);
                    if (stamp != 0L) {
                        // Put the key in the map on success.
                        mMap.put(key, value);

                        // Unlock the write lock.
                        mStampedLock.unlockWrite(stamp);

                        // Return the value.
                        return value;
                    } else {
                        // Revert to conditional write strategy.
                        return computeIfAbsentConditionalWrite(key,
                                                               mappingFunction);
                    }
                }
            }
        }

        // Return the value (either new or old).
        return value;
    }

    /**
     * Try to get the value from the map via {@code key} or apply
     * {@code mappingFunction} if value's not yet computed.
     */
    private V getOrComputeValueUnlocked(K key,
                                        Function<? super K,? extends V> mappingFunction) {
        // Get the current value (if any).
        V value = mMap.get(key);

        // If value is null then this is the first time in.
        if (value == null) 
            // Apply the mapping function to compute the value.
            value = mappingFunction.apply(key);
        
        // Return the value.
        return value;
    }
}
