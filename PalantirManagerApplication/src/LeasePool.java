import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * @class LeasePool
 *
 * @brief Defines a leasing mechanism that provides concurrent access
 *        to a fixed number of available resources.  Each resource can
 *        be leased for a designated amount of time.  When the lease
 *        expires the Thread that acquired the lease will be sent an
 *        interrupt request, which will cause it to receive the
 *        InterruptedException.
 *
 *        This class implements a variant of the "Pooling" pattern in
 *        POSA3.  It also implements the GoF Bridge and Strategy
 *        patterns to configure the type of synchronization mechanism
 *        used to protect internal state from race conditions.
 */
public class LeasePool<R> {
    enum SyncStrategy {
        SYNCHRONIZED, // Synchronized statement
        RWLOCK,       // ReentrantReadWriteLock
        STAMPEDLOCK   // StampedLock
    }

    /**
     * Strategy for managing access to LeasePool internal state.  This
     * plays the role of the "Strategy" in the Strategy pattern and
     * the root of the "Implementor" hierarchy in the Bridge pattern.
     */
    private final LeasePoolStrategy<R> mLeasePoolStrategy;

    /**
     * Constructor creates a LeasePool for the List of @a resources
     * passed as a parameter.  The @a SyncStrategy determines the type
     * synchronization strategy used to protect the internal data
     * structures of the LeasePool implementation.
     */
    LeasePool(List<R> resources,
              SyncStrategy syncStrategy) {
        // The makeStrategy() factory method creates the designated
        // type of synchronization strategy.
        mLeasePoolStrategy =
            LeasePoolStrategy.makeStrategy(resources,
                                           syncStrategy);
    }

    /**
     * Get the next available resource from the LeasePool, blocking
     * until one is available.  @code leaseDurationInMillis specifies
     * the maximum amount of time the lease for the resource will be
     * valid.  When this time is expires the Thread that acquired the
     * lease will be sent an interrupt request, which will cause it to
     * receive the InterruptedException.
     */
    public R acquire(long leaseDurationInMillis) {
        // Simply forward to the corresponding implementor method.
        return mLeasePoolStrategy.acquire(leaseDurationInMillis);
    }

    /**
     * Returns the designated @code resource so that it's available
     * for other Threads to use.
     */
    public void release(final R resource) {
        // Simply forward to the corresponding implementor method.
        mLeasePoolStrategy.release(resource);
    }

    /**
     * Returns the amount of time (in milliseconds) remaining on the
     * lease.
     */
    public long remainingLeaseDuration(R resource) {
        // Simply forward to the corresponding implementor method.
        return mLeasePoolStrategy.remainingLeaseDuration(resource);
    }
}

