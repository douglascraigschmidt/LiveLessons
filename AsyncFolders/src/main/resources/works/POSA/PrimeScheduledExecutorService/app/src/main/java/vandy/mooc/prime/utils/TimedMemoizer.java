package vandy.mooc.prime.utils;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
     * This map associates a key K with a value V that's produced by a
     * function.  A RefCountedValue is used to keep track of how many
     * times a key/value pair is accessed.
     */
    private final ConcurrentMap<K, RefCountedValue<V>> mCache =
            new ConcurrentHashMap<>();

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * The amount of time to retain a value in the cache.
     */
    private final long mTimeoutInMillisecs;

    /**
     * Executor service that executes a runnable after a given timeout
     * to remove expired keys.
     */
    private ScheduledExecutorService mScheduledExecutorService = 
        Executors.newScheduledThreadPool(1);

    /**
     * An object with ref count of 1 indicates its key hasn't been
     * accessed in mTimeoutInMillisecs.
     */
    private final RefCountedValue<?> mNonAccessedValue =
            new RefCountedValue<>(null, 1);

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
         * A scheduled future that can be used to cancel a runnable
         * that has been scheduled to run at a fixed interval to check
         * if this RefCountedValue has become stale and should be
         * removed from the cache (see scheduleAtFixedRateTimeout()).
         */
        ScheduledFuture<?> mScheduledFuture;

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
            @SuppressWarnings("unchecked")
                final RefCountedValue<V> t =
                (RefCountedValue<V>) obj;
            return mRefCount.get() == t.mRefCount.get();
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

        /**
         * Use the ScheduledExecutorService to remove @a key from the
         * cache if its timeout expires and it hasn't been accessed in
         * mTimeoutInMillisecs.
         */
        void schedule(K key) {
            // Create a runnable that will check if the cached entry
            // has become stale (i.e., not accessed within
            // mTimeoutInMillisecs) and if so will remove that entry.
            final Runnable removeIfStale = new Runnable() {
                    @Override
                    public void run() {
                        // Store the current ref count.
                        long oldCount = mRefCount.get();

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
                        } else {
                            Log.d(TAG,
                                  "key "
                                  + key
                                  + " NOT removed from cache since it was accessed recently ("
                                  + mRefCount.get()
                                  + ") and ("
                                  + mNonAccessedValue.mRefCount.get()
                                  + ")");

                            if (mCache.get(key) == null)
                                Log.d(TAG, "key not in cache");
                            else
                                Log.d(TAG, "key IS in cache");

                            // Try to reset ref count to 1 so that it
                            // won't be considered as accessed (yet).
                            // However, if ref count has increased
                            // between the call to remove() and here
                            // don't reset it to 1.
                            mRefCount.getAndUpdate(curCount -> 
                                                   curCount > oldCount ? curCount : 1);

                            // Reschedule this runnable to run again
                            // in mTimeoutInMillisecs.
                            mScheduledExecutorService.schedule
                                (this,
                                 mTimeoutInMillisecs,
                                 TimeUnit.MILLISECONDS);
                        }
                    }
                };

            // Schedule runnable to execute after mTimeoutInMillisecs.
            mScheduledExecutorService.schedule
                (removeIfStale,
                 mTimeoutInMillisecs,
                 TimeUnit.MILLISECONDS);
        }

        /**
         * Use the ScheduledExecutorService to remove @a key from the
         * cache if its timeout expires and it hasn't been accessed in
         * mTimeoutInMillisecs.
         */
        void scheduleAtFixedRateTimeout(K key) {
            // This runnable is used to cancel the period timer if the
            // key hasn't been accessed recently.
            Runnable cancelRunnable = () -> {
                // Store the current ref count.
                long oldCount = mRefCount.get();

                // Remove the key only if it hasn't been accessed in
                // mTimeoutInMillisecs.
                if (mCache.remove(key, mNonAccessedValue)) {
                    Log.d(TAG,
                          "key "
                          + key
                          + " removed from cache (" 
                          + mCache.size()
                          + ") since it wasn't accessed recently");

                    // Stop the ScheduledExecutorService from
                    // re-dispatching this runnable.
                    mScheduledFuture.cancel(true);
                } else {
                    Log.d(TAG,
                          "key "
                          + key
                          + " NOT removed from cache ("
                          + mCache.size() + ") since it was accessed recently ("
                          + mRefCount.get()
                          + ") and ("
                          + mNonAccessedValue.mRefCount.get()
                          + ")");
                    assert(mCache.get(key) != null);

                    // Try to reset ref count to 1 so that it won't be
                    // considered as accessed (yet).  However, if ref
                    // count has increased between the call to
                    // remove() and here don't reset it to 1.
                    mRefCount.getAndUpdate(curCount -> 
                                           curCount > oldCount ? curCount : 1);
                }
            };

            // Schedule runnable to execute repeatedly every
            // mTimeoutInMillisecs.
            mScheduledFuture =
                mScheduledExecutorService.scheduleAtFixedRate
                (cancelRunnable,
                 mTimeoutInMillisecs, // Initial timeout.
                 mTimeoutInMillisecs, // Periodic timeout.
                 TimeUnit.MILLISECONDS);
        }
    }

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
                // Apply the function and store the result. 
                RefCountedValue<V> rcv =
                    new RefCountedValue<>(mFunction.apply(k),
                                          0);
                // The code below *must* be done here so that it's
                // protected by the ConcurrentHashMap lock.
                if (!Thread.currentThread().isInterrupted()
                    && mTimeoutInMillisecs > 0)
                    // Schedule removal of @a key from the cache if its
                    // timeout expires and it hasn't been accessed in
                    // mTimeoutInMillisecs.  Change this to
                    // rcValue.Task.schedule(key) to try another
                    // implementation.
                    // rcv.scheduleAtFixedRateTimeout(key);
                    rcv.schedule(key);
                    return rcv;
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

        // Iterate through all the keys in the map and cancel/remove
        // them all.
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
