import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @class LeasePoolStrategy
 *
 * @brief Defines the superclass for several sublcasses that provide
 *        differs ways to synchronize access to the internal state of
 *        the LeasePool, which plays the role of the "Abstraction" in
 *        the Bridge pattern.  Likewise, this class plays the role of
 *        the "Strategy" in the Strategy pattern, the root of the
 *        "Implementor" hierarchy in the Bridge pattern, and the role
 *        of the "Abstract Class" in the Template Method pattern.
 */
abstract public class LeasePoolStrategy<Resource> {
    /**
     * A counting Semaphore that limits concurrent access to the fixed
     * number of available resources managed by the @a LeasePool.
     */
    private final Semaphore mAvailableResources;

    /**
     * Associates the @a Resource key to the @a LeaseState values.
     */
    protected final AbstractMap<Resource, LeaseState> mResourceMap;

    /**
     * @class LeaseState
     *
     * @brief This class contains the values associated with a
     *        @code Resource key in the Java HashMap that are used to
     *        implement the timed lease semantics.
     */
    protected static class LeaseState implements AutoCloseable {
        /**
         * Create a ScheduledThreadPoolExecutor with size 1 to enforce
         * the lease expiration semantics.
         */
        private static ScheduledExecutorService sScheduledExecutorService 
            = Executors.newScheduledThreadPool(1);

        /**
         * Stores how long a Thread can hold a lease on a resource.
         */
        private long mLeaseDuration;

        /**
         * Keep track of the @a ScheduledFuture returned from the @a
         * ScheduledExecutorService.
         */
        private ScheduledFuture<?> mFuture;

        /**
         * Default constructor.
         */
        public LeaseState() {
            mLeaseDuration = 0;
            mFuture = null;
        }

        /**
         * Obtain a lease on @a Resource in the HashMap.
         *
         * @param leaseDurationInMillis 
         *            The (relative) duration of time the lease is held.
         */
        public LeaseState(long leaseDurationInMillis) {
            // The (absolute) time over which this Thread leases the
            // resource.
            mLeaseDuration =
                leaseDurationInMillis + System.currentTimeMillis();

            // Get the current Thread and set it as the Leasor of this
            // resource.
            final Thread leasorThread = Thread.currentThread();

            // This Runnable command is dispatched by the
            // ScheduledExecutorService after the lease expires to
            // interrupt the Thread that's leasing the resource.
            Runnable threadInterruptRunnable = new Runnable() {
                    @Override
                    public void run() {
                        leasorThread.interrupt();
                    }
                };

            // Schedule the Runnable command to fire at the absolute
            // mLeaseDurationInMillis time in the future.
            mFuture =
                sScheduledExecutorService.schedule(threadInterruptRunnable,
                                                   leaseDurationInMillis,
                                                   TimeUnit.MILLISECONDS);
        }

        /**
         * Release the lease by cleaning up the values associated with
         * a @a Resource key in the HashMap.
         */
        @Override
        public void close() {
            if (mFuture != null)
                // Cancel the timer associated with the
                // threadInterruptRunnable command.
                mFuture.cancel(true);
            else
                mFuture = null;
        }

        /**
         * Return the remaining lease duration.
         */
        public long remainingTime() {
            return mLeaseDuration - System.currentTimeMillis();
        }
    }

    /**
     * A special value that's used to indicate when a resource is not
     * in use.
     */
    protected final LeaseState mNotInUse = new LeaseState();

    /**
     * This factory method creates a LeasePoolStrategy object based on
     * the designated @a strategy parameter.
     */
    public static <Resource> LeasePoolStrategy<Resource> makeStrategy 
                                 (List<Resource> resources, 
                                  LeasePool.SyncStrategy strategy) {
        switch (strategy) {
        case SYNCHRONIZED:
            return new LeasePoolStrategySynchronized<Resource>
                (resources,
                 new HashMap<Resource, LeaseState>());
        case RWLOCK:
            return new LeasePoolStrategyRWLock<Resource>
                (resources,
                 new HashMap<Resource, LeaseState>());
        case STAMPEDLOCK:
            return new LeasePoolStrategyStampedLock<Resource>
                (resources,
                 new HashMap<Resource, LeaseState>());
        case CONCURRENT_HASHMAP:
            return new LeasePoolStrategyConcurrentHashMap<Resource>
                (resources,
                 new ConcurrentHashMap<Resource, LeaseState>());
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructor for the @a resources passed as a parameter.  This
     * constructor is protected to ensure the @code makeStrategy()
     * factory method is used.
     */
    protected LeasePoolStrategy
        (List<Resource> resources,
         AbstractMap<Resource, LeaseState> resourceMap) {
        // Initialize the HashMap.
    	mResourceMap = resourceMap;
    	
        // Insert the resources into the mResourceMap, using mNotInUse
        // to indicate that all resources are available for use.
        for (Resource key : resources) {
            mResourceMap.put(key,
                             mNotInUse);
        }

        // Create a counting Semaphore that uses a "fair"
        // (i.e., FIFO) policy.
        mAvailableResources = new Semaphore(resources.size(),
                                            true);
    }

    /**
     * Obtains a lease on a resource from the @a LeasePool, blocking
     * until the next resource is available.  The lease will be held
     * for @a leaseDurationInMillis milliseconds, after which point
     * the Thread holding the lease will receive an
     * InterruptedException.
     */
    public Resource acquire(long leaseDurationInMillis) {
        // This template method acquires the Semaphore and then
        // invokes the acquireHook() hook method to update the state
        // of the LeasePool.
        mAvailableResources.acquireUninterruptibly();
        return acquireHook(leaseDurationInMillis);
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected abstract Resource acquireHook(long leaseDurationInMillis);

    /**
     * Return the @code resource back to the @a LeasePool.
     */
    public void release(Resource resource) {
        // This template method invokes the releaseHook() hook method
        // to update the state of the LeasePool and then releases the
        // Semaphore.
        if (releaseHook(resource))
            mAvailableResources.release();
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected abstract boolean releaseHook(Resource resource);

    /**
     * Returns the amount of time remaining on the lease in
     * milliseconds.
     */
    public abstract long remainingTime(Resource resource);

    /**
     * Returns the amount of time remaining on the lease in
     * milliseconds.  This method factors out common code called by
     * subclasses of @a LeasePoolStrategy, which hold different types
     * of locks when this method is called.
     */
    protected long remainingTimeUnlocked(Resource resource) {
        // Get the LeaseState associated with the resource.
        LeaseState values = mResourceMap.get(resource);
        // Compute the remaining lease duration.
        return values.remainingTime();
    }
}
