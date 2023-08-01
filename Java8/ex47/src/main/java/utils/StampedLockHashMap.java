package utils;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

/**
 * A concurrent {@link Map} collection that's implemented using
 * advanced features of {@link StampedLock} to protect mutable shared
 * state.  This implementation just focuses on the {@code
 * computeIfAbsent()} method, so other methods are omitted for
 * brevity.
 * 
 * If someone wants to implement the other {@link Map} methods via
 * {@link StampedLock} please feel free to contribute to the project!
 */
public class StampedLockHashMap<K, V>
       extends AbstractMap<K, V> 
       implements Map<K, V> {
    /**
     * The {@link Map} that's used to implement the {@link
     * StampedLockHashMap}.
     */
    private final Map<K, V> mMap;

    /**
     * The {@link StampedLock} instance used to protect the {@link
     * HashMap}.
     */
    private final StampedLock mSLock;

    /**
     * Constructor initializes the fields.
     */
    public StampedLockHashMap(){
        mMap = new HashMap<>();
        mSLock = new StampedLock();
    }

    /**
     * @return The number of elements in this {@link Map}
     */
    @Override
    public int size() {
        return mMap.size();
    }

    /**
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * Removes all the elements from this collection.  The
     * collection will be empty after this method returns.
     */
    @Override
    public void clear(){
        mMap.clear();
    }

    /**
     * Return the {@link Set} containing all entries in the {@link Map}.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return mMap.entrySet();
    }

    /**
     * If {@code key} is not already associated with a value (or is
     * mapped to null) then compute its value using the given {@code
     * mappingFunc} and enter it into the {@link Map} (unless it's
     * null).
     *
     * @param key The key to be mapped
     * @param mappingFunc The {@link Function} that maps the key to the
     *                    value that maps the key to the value
     */
    public V computeIfAbsent
        (K key,
         Function<? super K, ? extends V> mappingFunc) {
        // Determine the appropriate strategy to use!
        return switch (Options.instance().stampedLockStrategy()) {
        case 'W' -> computeIfAbsentWriteLock(key, mappingFunc);
        case 'C' -> computeIfAbsentConditionalWrite(key, mappingFunc);
        case 'O' -> computeIfAbsentOptimisticRead(key, mappingFunc);
        default -> throw new IllegalArgumentException();
        };
    }

    /**
     * This implementation uses a conventional write lock, which is a
     * pessimistic lock.
     *
     * @param key The key to be mapped
     * @value mapping The {@link Function} that maps the key to the
     *                value that maps the key to the value
     */
    private V computeIfAbsentWriteLock
        (K key,
         Function<? super K, ? extends V> mappingFunction) {
        // Acquire the lock for writing.
        long stamp = mSLock.writeLock();

        // The following code is accessed exclusively by the
        // thread that owns the writelock!
        try {
            // Get the current value (if any).
            V value = mMap.get(key);

            // This is the slow path, i.e., the key does not have a
            // value associated with it in the map.
            if (value == null) {
                // Apply the mapping function.
                value = mappingFunction.apply(key);

                // If mapping function worked, then add value to map.
                if (value != null) {
                    // Put the key in the map on success.
                    mMap.put(key, value);
                }
            }

            // Return the value (either old or new).
            return value;
        } finally {
            // Unlock the write stamp.
            mSLock.unlockWrite(stamp);
        }
    }

    /**
     * This implementation uses a conditional write lock, which is a
     * bit more optimistic than a conventional write lock.
     *
     * @param key The key to be mapped
     * @value mapping The {@link Function} that maps the key to the
     *                value that maps the key to the value
     */
    private V computeIfAbsentConditionalWrite
        (K key,
         Function<? super K, ? extends V> mappingFunction) {
        // Acquire the lock for reading.
        long stamp = mSLock.readLock();

        // This code can be executed by multiple threads that
        // share the readlock!
        try {
            // Try to get the value from the map via key.
            V value = mMap.get(key);

            // If a value's associated with the key, just return it.
            if (value != null)
                // No need for a write lock!
                return value;
            else {
                // Use a for-ever loop to avoid redundant code.
                for (long ws;;) {
                    // Try upgrade to writelock (non-blocking), where
                    // ws is non-zero on success.
                    if ((ws = mSLock.tryConvertToWriteLock(stamp)) != 0L) {
                        // Update stamp to ws.
                        stamp = ws;

                        // Apply mapping function to compute value.
                        if ((value = mappingFunction.apply(key)) != null)
                            // Put the key in the map on success.
                            mMap.put(key, value);

                        // Break out of the loop.
                        break;
                    } else {
                        // Release the read lock.
                        mSLock.unlockRead(stamp);

                        // Block acquiring the write lock.
                        stamp = mSLock.writeLock();

                        // Start over again with the write lock held.
                        if ((value = mMap.get(key)) != null)
                            break; // Key has a value so exit loop.
                    }
                }
            }

            // Return the value (either new or old).
            return value;
        } finally {
            // Unlock the stamp (which might be read or write).
            mSLock.unlock(stamp);
        }
    }

    /**
     * This implementation uses an optimistic read lock, which is
     * optimistic by its very nature ;-).
     *
     * @param key The key to be mapped
     * @value mapping The {@link Function} that maps the key to the
     *                value that maps the key to the value
     */
    private V computeIfAbsentOptimisticRead
        (K key,
         Function<? super K, ? extends V> mappingFunction) {
        // Initialize some local variables.
        long stamp = 0L;
        V value = null;
        int maxTries = Options.instance().maxTries();
        int tries = 0;

        // Try acquiring the lock optimistically a certain # of times.
        for (; tries < maxTries; tries++) {
            // "Acquire" the lock for optimistic reading.
            stamp = mSLock.tryOptimisticRead();

            // Get current value (if any) via optimistic read lock.
            value = mMap.get(key);

            // Break out if no writer occurred during this window.
            if (mSLock.validate(stamp))
                break;
        }

        // If we didn't get a valid value within maxTries iterations
        // then revert to conditional write strategy.
        if (tries == maxTries)
            return computeIfAbsentConditionalWrite(key,
                                                   mappingFunction);
        else if (value == null) {
            // This is the first time in for that key.

            // Try to upgrade the optimistic readlock to a writelock
            // (non-blocking).
            if ((stamp = mSLock.tryConvertToWriteLock(stamp)) == 0L)
                // Revert to conditional write strategy.
                return computeIfAbsentConditionalWrite(key,
                                                       mappingFunction);
            else
                try {
                    // Apply mapping function to compute the value.
                    if ((value = mappingFunction.apply(key)) != null)
                        // Put the key in the map on success.
                        mMap.put(key, value);
                } finally {
                    // Unlock the write lock.
                    mSLock.unlockWrite(stamp);
                }
        }

        // Return the value (either new or old).
        return value;
    }
}
