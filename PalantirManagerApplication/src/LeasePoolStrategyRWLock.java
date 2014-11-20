import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @class LeasePoolStrategyRWLock
 *
 * @brief This class uses a Java ReentrantReadWriteLock to synchronize
 *        access to the LeasePool's internal state.  It plays the role
 *        of the "Concrete Strategy" in the Strategy pattern, the
 *        "Concrete Implementor" in the Bridge pattern, and the
 *        "Concrete Class" in the Template Method pattern.
 */
public class LeasePoolStrategyRWLock<Resource>
       extends LeasePoolStrategy<Resource> {
    /**
     * Java ReentrantReadWriteLock that protects the LeasePool state.
     */
    private final ReentrantReadWriteLock mRWLock;

    /**
     * Constructor for the resources passed as a parameter.
     */
    LeasePoolStrategyRWLock
        (List<Resource> resources,
         AbstractMap<Resource, LeaseState> resourceMap) {
        // Call the superclass constructor.
        super(resources,
              resourceMap);
        
        // Initialize the ReentrantReadWriteLock.
        mRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // This implementation demonstrates ReentrantReadWriteLock's
        // support for downgrading a writeLock to a readLock.

        Lock lock = mRWLock.writeLock();

        // Acquire the writeLock.
        lock.lock();

        try {
            // Iterate through the HashMap, with writeLock initially held.
            for (Map.Entry<Resource, LeaseState> entry : mResourceMap.entrySet()) {

                // Check to see if the resource is in use.
                if (entry.getValue() == mNotInUse) {
                    // Initialize the LeaseState.
                    entry.setValue(new LeaseState(leaseDurationInMillis));

                    // Acquire the readLock.
                    final Lock readLock = mRWLock.readLock();
                    // Downgrade from a writeLock to a readLock.
                    readLock.lock(); 
                    try {
                        // Release writeLock.
                        lock.unlock();
                        // Only readLock is held when the resource is
                        // returned.
                        return entry.getKey();
                    } finally {
                        // Ensure readLock is released.
                        lock = readLock; 
                    }
                }
            }
        } finally {
            // Either release the writeLock or the readLock, depend on
            // what the lock variable references.
            lock.unlock();
        }

        // Should not be reached.
        return null; 
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected boolean releaseHook(Resource resource) {
        // Acquire the writeLock.
        mRWLock.writeLock().lock();

        // Put the mNotInUse value back into ConcurrentHashMap
        // for the resource key, which also atomically returns the
        // LeaseState associated with the resource.
        try (LeaseState values = mResourceMap.put(resource,
                                                  mNotInUse)) {
            // Return false if the @resource parameter was
            // previously the mNotInUse.
            return values != mNotInUse;
        } finally {
            // Release the writeLock.
            mRWLock.writeLock().unlock();

            // close() method of LeaseState clean ups the values
            // associated with a resource in the HashMap.
        }
    }

    /**
     * Returns the amount of time remaining on the lease.
     */
    public long remainingLeaseDuration(Resource resource) {
        mRWLock.readLock().lock();
        try {
            // Hold a read lock for the duration of this call.
            return remainingLeaseDurationUnlocked(resource);
        } finally {
            mRWLock.readLock().unlock();
        }
    }
}

