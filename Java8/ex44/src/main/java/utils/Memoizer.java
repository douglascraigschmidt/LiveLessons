package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that demonstrates how shared
 * mutable state can optimize the performance of a concurrent Java
 * program.  An instance of {@link Memoizer} maps a key to the value
 * produced by a {@link Function} argument passed to its constructor.
 * If a value has previously been computed it is returned rather than
 * calling the function to compute it again.
 *
 * The Java {@link ConcurrentHashMap} {@code computeIfAbsent()} method
 * is used to ensure only a single call to the function is run when a
 * key and value is first added to the cache, even when a {@link
 * Memoizer} is used in a concurrent/parallel program.
 *
 * This code is inspired by an example in "Java Concurrency in
 * Practice" by Brian Goetz et al.  More information on memoization is
 * available at <a
 * href="https://en.wikipedia.org/wiki/Memoization">this link</a>.
 */
public class Memoizer<K, V> {
    /**
     * This {@link Map} associates a key K with a value V that's
     * produced by a function.
     */
    private final Map<K, V> mCache;

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * Constructor initializes the fields.
     *
     * @param function The {@link Function} that produces a value
     *                 based on a key
     */
    public Memoizer(Function<K, V> function) {
        mFunction = function;
        mCache = new ConcurrentHashMap<>();
    }

    /**
     * Returns the value associated with the {@code key} in the {@link
     * Map}.  If there's no value associated with {@code key} then the
     * registered {@link Function} is called to create the value and
     * store it in the cache before returning it.

     * @param key The {@code key} in the {@link Map}
     * @return The value associated with {@code key} in cache
     */
    public V get(final K key) {
        return mCache
            // An atomic "check-then-act" method.
            .computeIfAbsent(key, mFunction);
    }

    /**
     * Removes the {@code} key (and its corresponding value) from this
     * memoizer.  This method does nothing if {@code key} is not in
     * the map.
     *
     * @param key The {@code key} to remove
     * @return The previous value associated with {@code key}, or null
     *         if there was no mapping for {@code key}
     */
    public V remove(K key) {
        return mCache.remove(key);
    }

    /**
     * @return The number of entries in the cache
     */
    public long size() {
        return mCache.size();
    }

    /**
     * @return The underlying {@link Map} used as a cache
     */
    public Map<K, V> getCache() {
        return mCache;
    }
}
