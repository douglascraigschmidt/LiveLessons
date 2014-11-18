import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * @class LeasePoolStrategyStampedLock
 *
 * @brief This class uses Java 8 StampedLocks to synchronize access to
 *        the LeasePool's internal state.  It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern, the "Concrete
 *        Implementor" in the Bridge pattern, and the "Concrete Class"
 *        in the Template Method pattern.
 */
public class LeasePoolStrategyStampedLock<Resource>
       extends LeasePoolStrategy<Resource> {
    /**
     * Java StampedLock that protects the LeasePool state.
     */
    private final StampedLock mStampedLock;

    /**
     * Constructor for the resources passed as a parameter.
     */
    LeasePoolStrategyStampedLock(List<Resource> resources) {
        // Call the superclass constructor.
        super(resources);

        // Initialize the StampedLock.
        mStampedLock = new StampedLock();
    }

    /**
     * Returns the amount of time remaining on the lease.
     */
    public long remainingLeaseDuration(Resource resource) {
        // Do an optimistic read.
        long stamp = mStampedLock.tryOptimisticRead();

        // No lock is actually held during this call.
        long remainingDuration =
            remainingLeaseDurationUnlocked(resource);

        // Check to see if a write lock has been acquired.
        if (mStampedLock.validate(stamp)) {
            // If not, then return the duration.
            return remainingDuration;
        } else {
            // Otherwise, acquire a readLock and get the remaining
            // lease duration.
            stamp = mStampedLock.readLock();
            try {
                return remainingLeaseDurationUnlocked(resource);
            } finally {
                // Release the readlock.
                mStampedLock.unlockRead(stamp);
            }
        }
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // Start out with a readLock.
        long stamp = mStampedLock.readLock();
        try {
            // Iterate through the HashMap, with readLock initially
            // held.
            for (Iterator<Map.Entry<Resource, Values>> entries =
                     mResourceMap.entrySet().iterator();
                 entries.hasNext();
                 ) {
                // Get the next entry in the HashMap.
                Map.Entry<Resource, Values> entry = entries.next();

                // Not currently in use.
                if (!entry.getValue().mInUse) {
                    // Get the resource key with readLock (or
                    // writeLock) held.
                    final Resource resource = entry.getKey();

                    // Attempt to upgrade to a writeLock.
                    long writeStamp =
                        mStampedLock.tryConvertToWriteLock(stamp);

                    // If the upgrade to a writeLock worked then we
                    // can initialize the Values since the critical
                    // section is protected from concurrent access.
                    if (writeStamp != 0) {
                        stamp = writeStamp;
                        // Initialize Values and return the resource.
                        entry.getValue().acquire(leaseDurationInMillis);
                        return resource;
                    } 
                    // Otherwise, fall back to using writeLock.
                    else {
                        // Release the readLock.
                        mStampedLock.unlockRead(stamp);

                        // Acquire a writeLock (this call may block).
                        stamp = mStampedLock.writeLock();

                        // Restart at the beginning of the HashMap
                        // since its state may have changed between
                        // the point where the readLock was released
                        // and the writeLock was acquired.
                        entries = mResourceMap.entrySet().iterator();
                    }
                }
            }
        } finally {
            // Unlock either the writeLock or the readLock, depending
            // on the value of stamp.
            mStampedLock.unlock(stamp);
        }
        return null;
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected boolean releaseHook(Resource resource) {
        // Acquire a writeLock.
        long stamp = mStampedLock.writeLock();

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
            mStampedLock.unlockWrite(stamp);
        }
    }
}
