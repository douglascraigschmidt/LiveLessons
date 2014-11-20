import java.util.AbstractMap;
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
    LeasePoolStrategyStampedLock
        (List<Resource> resources,
         AbstractMap<Resource, LeaseState> resourceMap) {
        // Call the superclass constructor.
        super(resources,
              resourceMap);

        // Initialize the StampedLock.
        mStampedLock = new StampedLock();
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // This implementation demonstrates StampedLock's
        // support for upgrading a readLock to a writeLock.

        // Start out with a readLock.
        long stamp = mStampedLock.readLock();
        try {
            // Iterate through the HashMap, with readLock initially
            // held.
            for (Iterator<Map.Entry<Resource, LeaseState>> entries =
                     mResourceMap.entrySet().iterator();
                 entries.hasNext();
                 ) {
                // Get the next entry in the HashMap.
                Map.Entry<Resource, LeaseState> entry = entries.next();

                // Check to see if the entry is in use.
                if (entry.getValue() == mNotInUse) {
                    // Get the resource key with readLock (or
                    // writeLock) held.
                    final Resource resource = entry.getKey();

                    // Attempt to upgrade to a writeLock.
                    long writeStamp =
                        mStampedLock.tryConvertToWriteLock(stamp);

                    // If the upgrade to a writeLock worked then we
                    // can initialize the LeaseState since the critical
                    // section is protected from concurrent access.
                    if (writeStamp != 0) {
                        stamp = writeStamp;
                        // Initialize LeaseState and return the resource.
                        entry.setValue(new LeaseState(leaseDurationInMillis));
                        return resource;
                    } 
                    // Otherwise, fall back to using writeLock.
                    else {
                        // Release the readLock.
                        mStampedLock.unlockRead(stamp);

                        // Acquire a writeLock (this call may block).
                        stamp = mStampedLock.writeLock();

                        // Restart at the beginning of the HashMap
                        // since its state may have changed during the
                        // window of time when the readLock was
                        // released and the writeLock was acquired.
                        entries = mResourceMap.entrySet().iterator();
                    }
                }
            }
        } finally {
            // Unlock either the writeLock or the readLock, depending
            // on the value of stamp.
            mStampedLock.unlock(stamp);
        }

        // Should not be reached.
        return null;
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected boolean releaseHook(Resource resource) {
        // Acquire a writeLock.
        long stamp = mStampedLock.writeLock();

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
            mStampedLock.unlockWrite(stamp);

            // close() method of LeaseState clean ups the values
            // associated with a resource in the HashMap.
        }
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
            // Otherwise, acquire a readLock (which may block) and get
            // the remaining lease duration.
            stamp = mStampedLock.readLock();
            try {
                return remainingLeaseDurationUnlocked(resource);
            } finally {
                // Release the readlock.
                mStampedLock.unlockRead(stamp);
            }
        }
    }
}

