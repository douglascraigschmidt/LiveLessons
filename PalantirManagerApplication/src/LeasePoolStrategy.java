import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @class LeasePoolStrategy
 *
 * @brief Contains various implementations of the LeasePool that
 *        provide differs ways to synchronize access to its internal
 *        state.
 *
 *        This class plays the role of the "Strategy" in the Strategy
 *        pattern, the root of the "Implementor" hierarchy in the
 *        Bridge pattern, and the role of the "Abstract Class" in the
 *        Template Method pattern.
 */
abstract public class LeasePoolStrategy<Resource> {
    /**
     * A counting Semaphore that limits concurrent access to the fixed
     * number of available resources.
     */
    private final Semaphore mAvailableResources;

    /**
     * @class Values
     *
     * @brief This class contains the values associated with a
     *        @code Resource key in the Java HashMap that are used to
     *        implement the LeasePool semantics.
     */
    class Values {
        /**
         * Initialize the values that are associated with a @a
         * Resource key in the HashMap.
         */
        public void acquire(long leaseDurationInMillis) {
            // Mark this resource as now being in use.
            mInUse = true;

            // Get the current Thread and set it as the Leasor of this
            // resource.
            final Thread leasorThread = Thread.currentThread();
            mLeasorThread = leasorThread;

            // This Runnable command is dispatched after the lease
            // expires to interrupt the Thread that's leasing the
            // resource.
            Runnable threadInterruptRunnable = new Runnable() {
                    @Override
                        public void run() {
                        leasorThread.interrupt();
                    }
                };

            // Schedule the Runnable command to fire at @a
            // leastDurationInMillis in the future.
            mFuture =
                mScheduledExecutorService.schedule(threadInterruptRunnable,
                                                   leaseDurationInMillis,
                                                   TimeUnit.MILLISECONDS);

            // The duration over which this Thread leases the
            // resource.
            mLeaseDuration =
                leaseDurationInMillis + System.currentTimeMillis();
        }

        /**
         * Cleanup the values associated with a @a Resource key in the
         * HashMap.
         */
        public void release() {
            // Mark the resource is no longer in use and cancel the
            // timer.
            mInUse = false;
            mFuture.cancel(true);
        }

        /**
         * Marks whether the resource is in use or not.
         */
        public boolean mInUse;

        /**
         * Marks the Thread that is currently leasing a resource.
         */
        public Thread mLeasorThread;

        /**
         * Keep track of the @a ScheduledFuture returned from the @a
         * ScheduledExecutorService.
         */
        public ScheduledFuture<?> mFuture;

        /**
         * The duration that a Thread can hold a lease on a resource.
         */
        public long mLeaseDuration;
    }

    /**
     * Maps the resource @a Resource to the @a Values.
     */
    protected final HashMap<Resource, Values> mResourceMap;

    /**
     * This object is used to enforce the lease expiration semantics.
     */
    protected final ScheduledExecutorService mScheduledExecutorService;

    /**
     * Constructor for the @a resources passed as a parameter.  This
     * constructor is protected to ensure the @code makeStrategy()
     * factory method is used.
     */
    protected LeasePoolStrategy(List<Resource> resources) {
        // Initialize the HashMap.
    	mResourceMap = new HashMap<Resource, Values>();
    	
        // Insert the resources into the Map.
        for (Resource key : resources) {
          mResourceMap.put(key, new Values());
        }

        // Create a counting Semaphore that's configured to use the
        // "fair" (round-robin) policy.
        mAvailableResources = new Semaphore(resources.size(),
                                            true);

        // Create a ScheduledThreadPoolExecutor that enforces the
        // lease expiration semantics.
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * This factory method creates a LeasePoolStrategy object based on
     * the designated @a strategy parameter.
     */
    public static <Resource> LeasePoolStrategy<Resource> makeStrategy 
                                 (List<Resource> resources, 
                                  LeasePool.SyncStrategy strategy) {
        switch (strategy) {
        case SYNCHRONIZED:
            return new LeasePoolStrategySynchronized<Resource>(resources);
        case RWLOCK:
            return new LeasePoolStrategyRWLock<Resource>(resources);
        case STAMPEDLOCK:
            return new LeasePoolStrategyStampedLock<Resource>(resources);
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Acquire a lease on a resource from the @a LeasePool, blocking
     * until a resource is available.  The lease will be held for @a
     * leaseDurationInMillis milliseconds, after which point the
     * Thread holding the lease will receive an InterruptedException.
     */
    public Resource acquire(long leaseDurationInMillis) {
        // This template method acquires the Semaphore and then
        // invokes the acquireHook() to update the state of the
        // LeasePool.
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
     * Hook method that returns the amount of time remaining on the
     * lease in milliseconds.
     */
    public abstract long remainingLeaseDuration(Resource resource);

    /**
     * Returns the amount of time remaining on the lease in
     * milliseconds.  This method factors out common code called by
     * subclasses of @a LeasePool, which hold various locks when this
     * method is called.
     */
    protected long remainingLeaseDurationUnlocked(Resource resource) {
        // Get the Values associated with the resource.
        Values values = mResourceMap.get(resource);
        return values.mLeaseDuration - System.currentTimeMillis();
    }
}
