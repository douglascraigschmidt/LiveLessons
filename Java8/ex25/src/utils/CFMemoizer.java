package utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that asynchronously maps a
 * key to a CompletableFuture to a value produced by a function.  If a
 * value has previously been computed it is returned rather than
 * calling the function to compute it again.  The Java
 * ConcurrentHashMap computeIfAbsent() method is used to ensure only a
 * single call to the function is run when a key and value is first
 * added to the cache.  This code is based on an example in "Java
 * Concurrency in Practice" by Brian Goetz et al. Memoization is
 * described at https://en.wikipedia.org/wiki/Memoization.
 */
public class CFMemoizer<K,V>
       implements Function<K, CompletableFuture<V>> {
    /**
     * This map associates a key K with a value CompletableFuture<V>
     * that's produced by a function.
     */
    private final ConcurrentHashMap<K, CompletableFuture<V>> mCache =
        new ConcurrentHashMap<>();

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mComputeFunction;

    /**
     * Constructor initializes the function field.
     */
    public CFMemoizer(Function<K, V> computeFunction) {
        mComputeFunction = computeFunction;
    }

    /**
     * Returns a CompletableFuture to a value associated with the key
     * in cache.  If there's no value associated with the key then the
     * function is called asynchronously to create the value and store
     * it in the cache.
     */
    @Override
    public CompletableFuture<V> apply(K key) {
        return mCache.computeIfAbsent(key,
                                      (k) ->
                                      CompletableFuture.supplyAsync(()->mComputeFunction.apply(k)));
    }
}
