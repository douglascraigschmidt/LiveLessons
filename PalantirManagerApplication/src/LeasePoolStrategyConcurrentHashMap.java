import java.util.AbstractMap;
import java.util.List;

/**
 * @class LeasePoolStrategyConcurrentHashMap
 *
 * @brief This class uses a Java ConcurrentHashMap to synchronize
 *        access to the LeasePool's internal state.  It plays the role
 *        of the "Concrete Strategy" in the Strategy pattern, the
 *        "Concrete Implementor" in the Bridge pattern, and the
 *        "Concrete Class" in the Template Method pattern.
 */
public class LeasePoolStrategyConcurrentHashMap<Resource>
       extends LeasePoolStrategy<Resource> {

    /**
     * Constructor for the resources passed as a parameter.
     */
    LeasePoolStrategyConcurrentHashMap
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
        LeaseState leaseState = new LeaseState(leaseDurationInMillis);

        // Iterate through the ConcurrentHashMap
        for (Resource r : mResourceMap.keySet())
            // Find the first key in the ConcurrentHashMap whose value
            // is the mNotInUse (which indicates it's available for
            // use) and atomically replace it with the new LeaseState.
            if (mResourceMap.replace(r,
                                     mNotInUse,
                                     leaseState))
                return r;

        // This shouldn't happen, but we need this here to make the
        // compiler happy.
        return null; 
    }

    /**
     * Hook method that releases the @code resource back to
     * the @LeasePool.
     */
    protected boolean releaseHook(Resource resource) {
        // Put the mNotInUse value back into ConcurrentHashMap for
        // the resource key, which also atomically returns the
        // LeaseState associated with the resource.
        try (LeaseState values = mResourceMap.put(resource,
                                                  mNotInUse)) {
            // Return false if the @resource parameter was previously
            // the mNotInUse.
            return values != mNotInUse;
        } finally {
            // close() method of LeaseState clean ups the values
            // associated with the resource in the ConcurrentHashMap.
        }
    }

    /**
     * Returns the amount of time remaining on the lease.
     */
    public long remainingLeaseDuration(Resource resource) {
        // This call doesn't require a lock since the
        // ConcurrentHashMap handles the synchronization.
        return remainingLeaseDurationUnlocked(resource);
    }
}

