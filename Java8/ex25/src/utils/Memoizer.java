package utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that synchronously maps a
 * key to the value produced by a function.  If a value has previously
 * been computed it is returned rather than calling the function to
 * compute it again.  The Java ConcurrentHashMap computeIfAbsent()
 * method is used to ensure only a single call to the function is run
 * when a key and value is first added to the cache.  This code is
 * based on an example in "Java Concurrency in Practice" by Brian
 * Goetz et al.  More information on memoization is available at
 * https://en.wikipedia.org/wiki/Memoization.
 */
public class Memoizer<K, V>
       implements Function<K, V> {
    /**
     * This map associates a key K with a value V that's produced by a
     * function.
     */
    private final ConcurrentMap<K, V> mCache =
            new ConcurrentHashMap<>();

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * Constructor initializes the function field.
     */
    public Memoizer(Function<K, V> function) {
        mFunction = function; 
    }

    /**
     * Returns the value associated with the key in cache.  If there's
     * no value associated with the key then the function is called to
     * create the value and store it in the cache before returning it.
     */
    public V apply(final K key) {
        return mCache.computeIfAbsent(key, mFunction::apply);
    }
}
