package leasepool;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * @class LeasePoolStrategySynchronized
 *
 * @brief This class uses Java synchronized statements to synchronize
 *        access to the LeasePool's internal state.  It plays the role
 *        of the "Concrete Strategy" in the Strategy pattern, the
 *        "Concrete Implementor" in the Bridge pattern, and the
 *        "Concrete Class" in the Template Method pattern.
 */
public class LeasePoolStrategySynchronized<Resource>
       extends LeasePoolStrategy<Resource> {
    /**
     * Constructor for the resources passed as a parameter.
     */
    LeasePoolStrategySynchronized
        (List<Resource> resources,
         AbstractMap<Resource, LeaseState> resourceMap) {
        // Call the superclass constructor.
        super(resources,
              resourceMap);
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // Create a new LeaseState object.
        LeaseState leaseState =
            new LeaseState(leaseDurationInMillis);

        // Acquires the intrinsic lock for the rest of this call.
        synchronized(this) {
            // Iterate through the HashMap.
            for (Resource resource : mResourceMap.keySet())
                // Find the first key in the HashMap whose value is
                // the mNotInUse (which indicates it's available for
                // use) and replace it with the new LeaseState.
                if (mResourceMap.replace(resource,
                                         mNotInUse,
                                         leaseState))
                    return resource;
        }

        // This shouldn't happen, but we need this here to make the
        // compiler happy.
        return null; 
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected boolean releaseHook(Resource resource) {
        // Hold the intrinsic lock for the duration of this call.
        synchronized(this) {
            // Put the mNotInUse value back into ConcurrentHashMap
            // for the resource key, which also atomically returns the
            // LeaseState associated with the resource.
            try (LeaseState values = mResourceMap.put(resource,
                                                      mNotInUse)) {
                // Return false if the @resource parameter was
                // previously the mNotInUse.
                return values != mNotInUse;
            } finally {
                // close() method of LeaseState clean ups the values
                // associated with a resource in the HashMap.
            }
        }
    }

    /**
     * Returns the amount of time remaining on the lease.
     */
    public long remainingTime(Resource resource) {
        // Hold the intrinsic lock for the duration of this call.
        synchronized(this) {
            return remainingTimeUnlocked(resource);
        }
    }
}

