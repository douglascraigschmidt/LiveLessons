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
    LeasePoolStrategyRWLock(List<Resource> resources) {
        // Call the superclass constructor.
        super(resources);
        
        // Initialize the ReentrantReadWriteLock.
        mRWLock = new ReentrantReadWriteLock();
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

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // This implementation demonstrates ReentrantReadWriteLock
        // downgrading.
        Lock lock = mRWLock.writeLock();

        // Acquire the writeLock.
        lock.lock();

        try {
            // Iterate through the HashMap, with writeLock initially held.
            for (Map.Entry<Resource, Values> entry : mResourceMap.entrySet()) {

                // Check to see if the resource is in use.
                if (!entry.getValue().mInUse) {
                    // Initialize the Values.
                    entry.getValue().acquire(leaseDurationInMillis);

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
            // what lock references.
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

        try {
            // Get the Values associated with the resource.
            Values values = mResourceMap.get(resource);
            if (values == null)
                // Return false if the @resource parameter is invalid.
                return false;
            else {
                // Cleanup the values associated with a Resource key
                // in the HashMap.
                values.release();
                return true;
            }
        } finally {
            // Release the writeLock.
            mRWLock.writeLock().unlock();
        }
    }
}

