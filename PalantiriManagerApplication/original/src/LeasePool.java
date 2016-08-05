import java.util.List;

/**
 * @class LeasePool
 *
 * @brief Defines a leasing mechanism that mediates concurrent access
 *        to a fixed number of available resources.  Each resource can
 *        be leased for a designated amount of time.  If the lease
 *        expires before the Thread that acquired it has released it,
 *        this Thread will be sent an interrupt request that will
 *        cause it to receive the InterruptedException so it can
 *        release the lease.
 *
 *        This class implements a variant of the "Pooling" pattern
 *        (kircher-schwanninger.de/michael/publications/Pooling.pdf).
 *        It plays the role of the "Abstraction" in the GoF Bridge and
 *        uses the Strategy pattern to configure the type of
 *        synchronization mechanism used to protect internal state
 *        from race conditions.
 */
public class LeasePool<Resource> {
    /**
     * Various types of synchronization mechanisms that can be
     * configured to control access to internal LeasePool state.
     */
    enum SyncStrategy {
        SYNCHRONIZED, // Synchronized statement
        RWLOCK,       // ReentrantReadWriteLock
        STAMPEDLOCK,   // StampedLock
        CONCURRENT_HASHMAP // ConcurrentHashMap
    }

    /**
     * Strategy for managing access to LeasePool internal state.  This
     * plays the role of the "Strategy" in the Strategy pattern and
     * the root of the "Implementor" hierarchy in the Bridge pattern.
     */
    private final LeasePoolStrategy<Resource> mLeasePoolStrategy;

    /**
     * Constructor creates a LeasePool for the List of @a resources
     * passed as a parameter.  The @a SyncStrategy determines the type
     * synchronization mechanism used to protect the internal data
     * structures of the LeasePool implementation.
     */
    LeasePool(List<Resource> resources,
              SyncStrategy syncStrategy) {
        // The makeStrategy() factory method creates the concrete
        // implementor of LeasePool that applies the designated type
        // of synchronization mechanism.
        mLeasePoolStrategy =
            LeasePoolStrategy.makeStrategy(resources,
                                           syncStrategy);
    }

    /**
     * Get a resource from the LeasePool, blocking until one is
     * available.  The @code leaseDurationInMillis parameter specifies
     * the maximum amount of time the lease for the resource will be
     * valid.  When this time is expires the Thread that acquired the
     * lease will be sent an interrupt request, which will cause it to
     * receive the InterruptedException.
     */
    public Resource acquire(long leaseDurationInMillis) {
        // Simply forward to the corresponding implementor method.
        return mLeasePoolStrategy.acquire(leaseDurationInMillis);
    }

    /**
     * Returns the designated @code resource to the @a LeasePool so
     * that it's available for other Threads to use.
     */
    public void release(final Resource resource) {
        // Simply forward to the corresponding implementor method.
        mLeasePoolStrategy.release(resource);
    }

    /**
     * Returns the amount of time (in milliseconds) remaining on the
     * lease held on the @a resource.
     */
    public long remainingTime(Resource resource) {
        // Simply forward to the corresponding implementor method.
        return mLeasePoolStrategy.remainingTime(resource);
    }
}

