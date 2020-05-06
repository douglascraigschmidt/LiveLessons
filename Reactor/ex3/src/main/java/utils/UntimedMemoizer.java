package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that synchronously maps a
 * key to the value produced by a {@code function} argument passed to
 * the constructor.  If a value has previously been computed it is
 * returned rather than calling the function to compute it again.  The
 * Java Map computeIfAbsent() method is used to ensure only a single
 * call to the function is run when a key and value is first added to
 * the cache.
 *
 * This code is inspired by an example in "Java Concurrency in
 * Practice" by Brian Goetz et al.  More information on memoization is
 * available at https://en.wikipedia.org/wiki/Memoization.
 */
public class UntimedMemoizer<K, V>
       extends Memoizer<K, V> {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG =
        getClass().getSimpleName();

    /**
     * This map associates a key K with a value V that's produced by a
     * function.
     */
    private final Map<K, V> mCache;

    /**
     * Constructor initializes the fields.
     *
     * @param function The function that produces a value based on a
     *                 key
     */
    public UntimedMemoizer(Function<K, V> function) {
        super(function);
        mCache = new ConcurrentHashMap<>();
    }

    /**
     * Returns the value associated with the key in cache.  If there's
     * no value associated with the key then the function is called to
     * create the value and store it in the cache before returning it.
     */
    public V apply(final K key) {
        return mCache.computeIfAbsent(key, mFunction);
    }

    /**
     * Removes the key (and its corresponding value) from this
     * memoizer.  This method does nothing if the key is not in the
     * map.
     *
     * @param key The key to remove
     * @ @return The previous value associated with key, or null if
     * there was no mapping for key.
     */
    public V remove(K key) {
        return mCache.remove(key);
    }

    /**
     * @return The number of keys in the cache.
     */
    public long size() {
        return mCache.size();
    }

    /**
     * @return A map containing the key/value entries in the cache.
     */
    public Map<K, V> getCache() {
        // Create a new concurrent hash map that contains
        // a copy of the cache.
        return new ConcurrentHashMap<>(mCache);
    }

    /**
     * Shutdown the memoizer.
     */
    @Override
    public void shutdown() {
        // Remove all the keys/values in the map.
        mCache.clear();
    }
}
