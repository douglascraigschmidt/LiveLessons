package vandy.mooc.prime.utils;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that maps a key to the value
 * produced by a function.  If a value has previously been computed it
 * is returned rather than calling the function to compute it again.
 * The ConcurrentHashMap computeIfAbsent() method is used to ensure
 * only a single call to the function is run when a key/value pair is
 * first added to the cache.  The Java ScheduledExecutor class is used
 * to scalably limit the amount of time a key/value is retained in the
 * cache.  This code is based on an example in "Java Concurrency in
 * Practice" by Brian Goetz et al.  More information on memoization is
 * available at https://en.wikipedia.org/wiki/Memoization.
 */
public class TimedMemoizer<K, V>
       implements Function<K, V> {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG =
        getClass().getSimpleName();

    /**
     * Keeps track of the number of times a key/value is referenced
     * within mTimeoutInMillisecs.
     */
    private class RefCountedValue<V> {
        /**
         * Keeps track of the number of times a key is referenced
         * within mTimeoutInMillisecs.
         */
        final AtomicLong mRefCount;

        /**
         * The value that's being reference counted.
         */
        final V mValue;

        /**
         * Constructor initializes the fields.
         */
        RefCountedValue(V value, long initialCount) {
            mValue = value;
            mRefCount = new AtomicLong(initialCount);
        }

        /**
         * Returns true if the ref counts are equal, else false.
         */
        @Override
        public boolean equals(Object obj) {
            if (getClass() != obj.getClass())
                return false;
            else {
                @SuppressWarnings("unchecked")
                final RefCountedValue<V> t =
                    (RefCountedValue<V>) obj;
                return mRefCount.get() == t.mRefCount.get();
            }
        }

        /**
         * Increments the ref count atomically and returns the value.
         */
        V get() {
            // Increment ref count atomically.
            mRefCount.getAndIncrement();

            // Return the value;
            return mValue;
        }
    }

    /**
     * An object with ref count of 1 indicates its key hasn't been
     * accessed in mTimeoutInMillisecs.
     */
    private final RefCountedValue<?> mNonAccessedValue =
        new RefCountedValue<>(null, 1);

    /**
     * This map associates a key K with a value V that's produced by a
     * function.  A RefCountedValue is used to keep track of how many
     * times a key/value pair is accessed.
     */
    private final ConcurrentMap<K, RefCountedValue<V>> mCache =
            new ConcurrentHashMap<>();

    /**
     * Keeps track of the number of entries in mCache so mPurgeEntries
     * can be properly scheduled and cancelled.
     */
    private final ThresholdCrosser mCacheCount = 
        new ThresholdCrosser(0);

    /**
     * A scheduled future that can be used to cancel a runnable that
     * has been scheduled to run at a fixed interval to check if
     * entries in the ConcurrentHashMap have become stale and should
     * be removed from the cache.
     */
    private ScheduledFuture<?> mScheduledFuture;

    /** 
     * This runnable will purge entries in the map that haven't been
     * accessed recently.
     */
    private final Runnable mPurgeEntries = () -> {
        Log.d(TAG,
              "start the purge of keys not accessed recently");

        // Iterate through all the keys in the map and purge those
        // that haven't been accessed recently.
        for (ConcurrentMap.Entry<K, RefCountedValue<V>> e : mCache.entrySet()) {
            final K key = e.getKey();
            final RefCountedValue<V> value = e.getValue();

            // Store the current ref count.
            long oldCount = value.mRefCount.get();

            // Remove the key only if it hasn't been
            // accessed in mTimeoutInMillisecs.
            if (mCache.remove(key,
                              mNonAccessedValue)) {
                Log.d(TAG,
                      "key "
                      + key
                      + " removed from cache (" 
                      + mCache.size()
                      + ") since it wasn't accessed recently");

                // Decrement the count of cached entries by one, which
                // will invoke the lambda when the count drops to 0.
                mCacheCount.decrementAndCallAtN
                    (0,
                     () -> {
                        // If there are no entries in the cache cancel
                        // mPurgeEntries from being called henceforth.
                        mScheduledFuture.cancel(true);
                        Log.d(TAG,
                              "cancelling mPurgeEntries");
                    });
            } else {
                Log.d(TAG,
                      "key "
                      + key
                      + " NOT removed from cache ("
                      + mCache.size() + ") since it was accessed recently ("
                      + value.mRefCount.get()
                      + ") and ("
                      + mNonAccessedValue.mRefCount.get()
                      + ")");
                assert(mCache.get(key) != null);

                // Try to reset ref count to 1 so that it won't be
                // considered as accessed (yet).  However, if ref
                // count has increased between the call to remove()
                // and here don't reset it to 1.
                value.mRefCount.getAndUpdate(curCount ->
                                             curCount > oldCount
                                             ? curCount
                                             : 1);
            }
        }

        Log.d(TAG,
              "ending the purge of keys not accessed recently");
    };

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * The amount of time to retain a value in the cache.
     */
    private final long mTimeoutInMillisecs;

    /**
     * ScheduledExecutorService executes a runnable after a given
     * timeout to remove expired keys.
     */
    private ScheduledExecutorService mScheduledExecutorService
        = new ScheduledThreadPoolExecutor(1);

    /**
     * Constructor initializes the fields.
     */
    public TimedMemoizer(Function<K, V> function,
                         long timeoutInMillisecs) {
        // Store the function for subsequent use.
        mFunction = function; 

        // Store the timeout for subsequent use.
        mTimeoutInMillisecs = timeoutInMillisecs;

        // Set the policies to clean everything up on shutdown.
        ScheduledThreadPoolExecutor exec =
                (ScheduledThreadPoolExecutor) mScheduledExecutorService;
        exec.setRemoveOnCancelPolicy(true);
        exec.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        exec.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    /**
     * Returns the value associated with the key in cache.  If there
     * is no value associated with the key then the function is called
     * to create the value and store it in the cache before returning
     * it.  A key/value entry will be purged from the cache if it's
     * not used within the timeout passed to the constructor.
     */
    public V apply(K key) {
        // Try to find the key in the cache.  If the key isn't present
        // then atomically compute the value associated with the key
        // and return a unique RefCountedValue associated with it.
        RefCountedValue<V> rcValue = mCache.computeIfAbsent
            (key,
             (k) -> {
                // Determine if this is the first entry added to the
                // cache after it was empty and invoke the lambda
                // expression if so.
                mCacheCount.incrementAndCallAtN
                (1,
                 () -> {
                    if (mScheduledExecutorService != null) {
                        Log.d(TAG,
                              "scheduling mPurgeEntries for key "
                              + key);

                        // Schedule a runnable to purge keys
                        // not accessed recently.
                        mScheduledFuture =
                        mScheduledExecutorService.scheduleAtFixedRate
                        (mPurgeEntries,
                         mTimeoutInMillisecs, // Initial timeout
                         mTimeoutInMillisecs, // Periodic timeout
                         TimeUnit.MILLISECONDS);
                    }});

                // Apply the function store/return the result.
                return new RefCountedValue<>(mFunction.apply(k),
                                             0);
             });

        // Return the value of the rcValue.
        return rcValue.get();
    }

    /**
     * Shutdown the TimedMemoizer and remove all the entries from its
     * ScheduledExecutorService.
     */
    public void shutdown() {
        // Reset the count.
        mCacheCount.setInitialCount(0);

        // Shutdown the ScheduledExecutorService.
        mScheduledExecutorService.shutdownNow();
        mScheduledExecutorService = null;

        // Iterate through keys in the map and cancel/remove them all.
        for (ConcurrentMap.Entry<K, RefCountedValue<V>> e : mCache.entrySet()) {
            final K key = e.getKey();
            final RefCountedValue<V> value = e.getValue();

            Log.d(TAG,
                  "key "
                  + key
                  + " and value "
                  + value.mValue
                  + " were removed from the TimedMemoizer cache");

            // Remove the key (and value) from the map.
            mCache.remove(key);
        }
    }
}
