import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @class LeasePoolStrategySynchronized
 *
 * @brief This class uses Java's synchronized statements to
 *        synchronize access to the LeasePool's internal state.  It
 *        plays the role of the "Concrete Strategy" in the Strategy
 *        pattern, the "Concrete Implementor" in the Bridge pattern,
 *        and the "Concrete Class" in the Template Method pattern.
 */
public class LeasePoolStrategySynchronized<Resource>
       extends LeasePoolStrategy<Resource> {
    /**
     * Constructor for the resources passed as a parameter.
     */
    LeasePoolStrategySynchronized(List<Resource> resources) {
        super(resources);
    }

    /**
     * Returns the amount of time remaining on the lease.
     */
    public long remainingLeaseDuration(Resource resource) {
        // Hold the intrinsic lock for the duration of this call.
        synchronized(this) {
            return remainingLeaseDurationUnlocked(resource);
        }
    }

    /**
     * Hook method that acquires a resource from the @a LeasePool.
     */
    protected Resource acquireHook(long leaseDurationInMillis) {
        // Hold the intrinsic lock for the duration of this call.
        synchronized(this) {
            // Iterate through the HashMap
            for (Map.Entry<Resource, Values> entry : mResourceMap.entrySet()) {

                // Check to see if the resource is in use.
                if (!entry.getValue().mInUse) {
                    // Initialize the Values.
                    entry.getValue().acquire(leaseDurationInMillis);
                    // Return the resource.
                    return entry.getKey();
                }
            }
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
        }
    }
}

