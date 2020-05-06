package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This abstract class defines a "memoizing" cache that synchronously
 * maps a key to the value produced by a {@code function} argument
 * passed to the constructor.  If a value has previously been computed
 * it is returned rather than calling the function to compute it
 * again.  The Java Map.computeIfAbsent() method is used to ensure
 * only a single call to the function is run when a key and value is
 * first added to the cache.
 */
public abstract class Memoizer<K, V>
       implements Function<K, V> {
    /**
     * This function produces a value based on the key.
     */
    protected final Function<K, V> mFunction;

    /**
     * Constructor initializes the fields.
     *
     * @param function The function that produces a value based on a
     *                 key
     */
    public Memoizer(Function<K, V> function) {
        mFunction = function;
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
    public abstract V remove(K key);

    /**
     * @return The number of keys in the cache.
     */
    public abstract long size();

    /**
     * @return A map containing the key/value entries in the cache.
     */
    public abstract Map<K, V> getCache();

    /**
     * Shutdown the memoizer.
     */
    public abstract void shutdown();
}
