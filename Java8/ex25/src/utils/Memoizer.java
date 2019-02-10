package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
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
 * The {@code map} argumented passed to the constructor determines
 * what implementation of {@code Map} is used for the cache.
 * Implementations include {@code ConcurrentHashMap} (which is correct
 * and efficient in a concurrent program), {@code SynchronizedMap}
 * (which is correct, but inefficient in a concurrent program), and
 * {@code HashMap} (which is incorrect in a concurrent program).
 *
 * This code is based on an example in "Java Concurrency in Practice"
 * by Brian Goetz et al.  More information on memoization is available
 * at https://en.wikipedia.org/wiki/Memoization.
 */
public class Memoizer<K, V>
       implements Function<K, V> {
    /**
     * This map associates a key K with a value V that's produced by a
     * function.
     */
    private final Map<K, V> mCache;

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * Constructor initializes the fields.
     *
     * @param function The function that produces a value based on a
     *                 key 
     * @map map The implementation of {@code Map} used to cache a
     *          value with its associated key  
     */
    public Memoizer(Function<K, V> function,
                    Map<K, V> map) {
        mFunction = function;
        mCache = map;
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
     * map. This method does nothing if the key is not in the map.
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
}
