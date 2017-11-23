package expressiontree.platspecs;

import expressiontree.platspecs.CommandLinePlatform;
import expressiontree.platspecs.Platform;

import java.util.HashMap;

/**
 * This class is a factory that is responsible for building the
 * designated @a Platform implementation at runtime.
 */
public class PlatformFactory {
    /** 
     * This interface uses the Command pattern to create @a Platform
     * implementations at runtime.
     */
    private static interface PlatformFactoryCommand {
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
     * Constructor that stores the objects that perform mInput and
     * output for a particular platform, such as CommandLinePlatform
     * or the AndroidPlatform.
     */
    public PlatformFactory(final Object input,
                           final Object output,
                           final Object activity) {
    	/** 
         * The "Sun Microsystems Inc." string maps to a command object
         * that creates an @a CommandLinePlatform implementation.
         */
        platformMap.put("Sun Microsystems Inc.",
                        new PlatformFactoryCommand() {
                            public Platform execute() {
                                return new CommandLinePlatform(input,
                                                               output);
                            }
                        });

    	/** 
         * The "Oracle Corporation" string maps to a command object
         * that creates an @a CommandLinePlatform implementation.
         */
        platformMap.put("Oracle Corporation", 
                        new PlatformFactoryCommand() {
                            public Platform execute() {
                                return new CommandLinePlatform(input,
                                                               output);
                            }
                        });
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
