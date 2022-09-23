package expressiontree.platspecs;

import java.util.HashMap;

/**
 * @class PlatformFactory
 * 
 * @brief This class is a factory that is responsible for building the
 *        designated @a Platform implementation at runtime.
 */
public class PlatformFactory {
    /** 
     * This interface uses the Command pattern to create @a Platform
     * implementations at runtime.
     */
    @FunctionalInterface
    private interface PlatformFactoryCommand {
        public Platform execute();
    }
	
    /**
     * HashMap used to map strings containing the Java platform names
     * and dispatch the execute() method of the associated @a Platform
     * implementation.
     */
    private HashMap<String, PlatformFactoryCommand> platformMap =
        new HashMap<>();
	
    /** 
     * Ctor that stores the objects that perform input and output for
     * a particular platform, such as CommandLinePlatform or the
     * AndroidPlatform.
     */
    public PlatformFactory(final Object input,
                           final Object output,
                           final Object activity) {
    	// The "The Android Project" string maps to a command object
        // that creates an @a AndroidPlatform implementation.
        platformMap.put("The Android Project",
                        // Receives the three parameters, input
                        // (EditText), output (TextView), mActivity
                        // (mActivity).
                        () -> new AndroidPlatform(input,
                                                  output,
                                                  activity));
    }

    /** 
     * Create a new {@code Platform} object based on underlying Java
     * platform.
     */
    public Platform makePlatform() {
        String name = System.getProperty("java.specification.vendor");

        return platformMap.get(name).execute();
    }
}
