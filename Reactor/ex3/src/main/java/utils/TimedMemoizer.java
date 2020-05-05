package utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * This class defines a "memoizing" cache that maps a key to the value
 * produced by a function.  If a value has previously been computed it
 * is returned rather than calling the function to compute it again.
 * The ConcurrentHashMap computeIfAbsent() method is used to ensure
 * only a single call to the function is run when a key/value pair is
 * first added to the cache.  The Java ScheduledExecutor class is used
 * to limit the amount of time a key/value is retained in the cache.
 * This code is based on an example in "Java Concurrency in Practice"
 * by Brian Goetz et al.  More information on memoization is available
 * at https://en.wikipedia.org/wiki/Memoization.
 */
public class TimedMemoizer<K, V>
       implements Function<K, V> {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG =
        getClass().getSimpleName();

    /**
     * A map associating a key K w/a value V produced by a function.
     * A RefCountedValue is used to keep track of how many times a
     * key/value pair is accessed during mTimeoutInMillisecs period.
     */
    private final ConcurrentHashMap<K, RefCountedValue> mCache;

    /**
     * The amount of time to retain a value in the cache.
     */
    private final long mTimeoutInMillisecs;

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * Executes a runnable after a given timeout to remove expired
     * keys.
     */
    private ScheduledExecutorService mScheduledExecutorService;

    /**
     * A ref count of 1 is used to check if a key's not been accessed
     * in mTimeoutInMillisecs.
     */
    private final RefCountedValue mNonAccessedValue =
        new RefCountedValue(null, 1);

    /**
     * Track # of times a key is referenced within
     * mTimeoutInMillisecs.
     */
    private class RefCountedValue {
        /**
         * Track # of times a key is referenced within
         * mTimeoutInMillisecs.
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
         * Increment the ref count atomically and return the value.
         */
        V get() {
            // Increment ref count atomically.
            mRefCount.getAndIncrement();

            // Return the value;
            return mValue;
        }

        /**
         * @return true if the ref counts are equal, else false.
         */
        @Override
        public boolean equals(Object obj) {
            if (getClass() != obj.getClass())
                return false;
            else {
                @SuppressWarnings("unchecked")
                    final RefCountedValue t = (RefCountedValue) obj;
                return mRefCount.get() == t.mRefCount.get();
            }
        }

        /**
         * Use the ScheduledExecutorService to schedule a runnable
         * that removes {@code key} from the cache if its timeout
         * expires and it hasn't been accessed in mTimeoutInMillisecs.
         */
        void schedule(K key) {
            // Runnable that checks if the cached entry became "stale"
            // (i.e., not accessed within mTimeoutInMillisecs) and if
            // so will remove that entry.
            Runnable removeIfStale = new Runnable() {
                    @Override
                    public void run() {
                        // Store the current ref count.
                        long oldCount = mRefCount.get();

                        // Remove the key only if it hasn't been
                        // accessed in mTimeoutInMillisecs.
                        if (mCache.remove(key,
                                          mNonAccessedValue)) {
                            Options.debug(
                                  "key "
                                  + key
                                  + " removed from cache (" 
                                  + mCache.size()
                                  + ") since it wasn't accessed recently");
                        } else {
                            Options.debug(
                                  "key "
                                  + key
                                  + " NOT removed from cache since it was accessed recently ("
                                  + mRefCount.get()
                                  + ") and ("
                                  + mNonAccessedValue.mRefCount.get()
                                  + ")");

                            if (mCache.get(key) == null)
                                Options.debug("key not in cache");
                            else
                                Options.debug("key IS in cache");

                            // Try to reset ref count to 1 so it won't
                            // be considered as accessed (yet).  Do
                            // NOT reset it to 1, however, if ref
                            // count has currently increased between
                            // remove() above and here.
                            mRefCount
                                .getAndUpdate(curCount -> 
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

            // Initially schedule runnable to execute after
            // mTimeoutInMillisecs.
            mScheduledExecutorService.schedule
                (removeIfStale,
                 mTimeoutInMillisecs,
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

        // Create a concurrent hash map.
        mCache = new ConcurrentHashMap<>();

        // Store the timeout for subsequent use.
        mTimeoutInMillisecs = timeoutInMillisecs;

        // Create a ScheduledThreadPoolExecutor with one thread.
        mScheduledExecutorService = 
            new ScheduledThreadPoolExecutor
                    (1,
                    // Make the thread a daemon so it shutsdown!
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });

        // Get an object to set policies that clean everything up on
        // shutdown.
        ScheduledThreadPoolExecutor exec =
            (ScheduledThreadPoolExecutor) mScheduledExecutorService;

        // Remove scheduled runnables on cancellation.
        exec.setRemoveOnCancelPolicy(true);

        // Disable periodic tasks at shutdown.
        exec.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        // Disable delayed tasks at shutdown.
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
        RefCountedValue rcValue = mCache.computeIfAbsent
            (key,
             (k) -> {
                // Apply the function and store the result. 
                RefCountedValue rcv =
                new RefCountedValue(mFunction.apply(k),
                                    0);
                // The code below *must* be done here so that it's
                // protected by the ConcurrentHashMap lock.
                if (!Thread.currentThread().isInterrupted()
                    && mTimeoutInMillisecs > 0)
                    // Schedule a runnable that removes key from the
                    // cache if its timeout expires and it hasn't been
                    // accessed in mTimeoutInMillisecs.
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
        // Shutdown the ScheduledExecutorService.
        mScheduledExecutorService.shutdownNow();
        mScheduledExecutorService = null;

        // Remove all the keys/values in the map.
        mCache.clear();
    }
}
