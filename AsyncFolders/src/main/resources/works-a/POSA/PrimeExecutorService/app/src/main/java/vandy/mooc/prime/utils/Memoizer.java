package vandy.mooc.prime.utils;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import static vandy.mooc.prime.utils.LaunderThrowable.launderThrowable;

/**
 * This class defines a "memoizing" cache that maps a key to the value
 * produced by a function.  If a value has previously been computed it
 * is returned rather than calling the function to compute it again.
 * The Java FutureTask class is used to ensure only a single call to
 * the function is run when a key and value is first added to the
 * cache.  This code is based on an example in "Java Concurrency in
 * Practice" by Brian Goetz et al.  More information on memoization is
 * available at https://en.wikipedia.org/wiki/Memoization.
 */
public class Memoizer<K, V>
       implements Function<K, V> {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * This map associates a key K with a value V that's produced by a
     * function.  A Future is used to ensure that the function is only
     * called once.
     */
    private final ConcurrentMap<K, Future<V>> mCache =
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
     * Returns the value associated with the key in cache.  If there
     * is no value associated with the key then the function is called
     * to create the value and store it in the cache before returning
     * it.
     */
    public V apply(final K key) {
        // Try to find the key in the cache.
        Future<V> future = mCache.get(key);

        // If the key isn't present we must compute its value.
        if (future == null) 
            future = computeValue(key);
        else
            Log.d(TAG,
                  "key "
                  + key
                  + "'s value was retrieved from the cache");

        // Return the value of the future, blocking until it's
        // computed.
        return getFutureValue(key, future);
    } 

    /**
     * Compute the value associated with the key and return a
     * unique RefCountedFutureTask associated with it.
     */
    private Future<V> computeValue(K key) {
        // Create a FutureTask whose run() method will compute the
        // value and store it in the cache.
        final FutureTask<V> futureTask =
            new FutureTask<>(() -> mFunction.apply(key));

        // Atomically try to add futureTask to the cache as the value
        // associated with key.
        Future<V> future = mCache.putIfAbsent(key, futureTask);

        // If future != null the value was already in the cache, so
        // just return it.
        if (future != null) {
            Log.d(TAG,
                  "key "
                  + key
                  + "'s value was added to the cache");
            return future;
        }
        // A value of null from put() indicates the key was just added
        // (i.e., it's the "first time in"), which indicates the value
        // hasn't been computed yet.
        else {
            // Run futureTask to compute the value, which is
            // (implicitly) stored in the cache when the computation
            // is finished.
            futureTask.run();

            // Return the future.
            return futureTask;
        }
    }

    /**
     * Return the value of the future, blocking until it's computed.
     */
    private V getFutureValue(K key,
                             Future<V> future) {
        try {
            // Get the result of the future, which will block if the
            // future hasn't finished running yet.
            return future.get();
        } catch (Exception e) {
            // Unilaterally remove the key from the cache when an
            // exception occurs.
            if (mCache.remove(key) != null)
                Log.d(TAG,
                      "key "
                      + key 
                      + " removed from cache upon exception");
            else
                Log.d(TAG,
                      "key "
                      + key 
                      + " NOT removed from cache upon exception");

            // Rethrow the exception.
            throw launderThrowable(e.getCause());
        }
    }
}
