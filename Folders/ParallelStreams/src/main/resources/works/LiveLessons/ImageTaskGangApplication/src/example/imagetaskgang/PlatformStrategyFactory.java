package example.imagetaskgang;

import java.util.HashMap;

/**
 * @class PlatformStrategyFactory
 * 
 * @brief This class is a Factory that uses the Command pattern to
 *        create the designated @a PlatformStrategy implementation
 *        (e.g., either an Android application or Java console
 *        application) at runtime.  The class plays the role of the
 *        Creator in the Factory Method pattern.  It also uses the
 *        Command pattern internally to efficiently create the
 *        appropriate type of @a PlatformStrategy subclass object.
 */
public class PlatformStrategyFactory {
    /**
     * Enumeration distinguishing platforms Android from plain ol' Java.
     */
    public enum PlatformType {
        ANDROID,
        PLAIN_JAVA
    };
    
    /**
     * Keep track of the type of platform.  This value won't change at
     * runtime.
     */
    private final PlatformType mPlatformType =
        System.getProperty("java.specification.vendor").indexOf("Android") >= 0
            ? PlatformType.ANDROID
            : PlatformType.PLAIN_JAVA;

    /**
     * This interface uses the Command pattern to create @a
     * PlatformStrategy implementations at runtime.
     */
    private interface IPlatformStrategyFactoryCommand {
        public PlatformStrategy execute();
    }

    /**
     * HashMap used to associate the PlatformType with the
     * corresponding command object whose execute() method creates the
     * appropriate type of @a PlatformStrategy subclass object.
     */
    private HashMap<PlatformType, IPlatformStrategyFactoryCommand> mPlatformStrategyMap =
        new HashMap<PlatformType, IPlatformStrategyFactoryCommand>();

    /**
     * Constructor stores the objects that perform output and
     * synchronization for a particular Java platform, such as
     * PlatformStrategyConsole or PlatformStrategyAndroid.
     */
    public PlatformStrategyFactory(final Object output) {
        if (mPlatformType == PlatformType.ANDROID)
            /**
             * Map the PlatformType of ANDROID to a command object that
             * creates an @a PlatformStrategyAndroid implementation.
             */
            mPlatformStrategyMap.put(PlatformType.ANDROID,
                                     new IPlatformStrategyFactoryCommand() {
                                         // Creates the PlatformStrategyAndroid.
                                         public PlatformStrategy execute() {
                                             return new PlatformStrategyAndroid(output);
                                         }
                                     });
        else if (mPlatformType == PlatformType.PLAIN_JAVA)
                 /**
                  * Map the PlatformType of PLAIN_JAVA to a command object that
                  * creates an @a ConsolePlatformStrategy implementation.
                  */
                 mPlatformStrategyMap.put(PlatformType.PLAIN_JAVA,
                                          new IPlatformStrategyFactoryCommand() {
                                              // Creates the PlatformStrategyConsole.
                                              public PlatformStrategy execute() {
                                                  return new PlatformStrategyConsole(output);
                                              }
                                          });
    }

    /**
     * Factory method that creates and returns a new @a
     * PlatformStrategy object based on underlying Java platform.
     */
    public PlatformStrategy makePlatformStrategy() {
        return mPlatformStrategyMap.get(mPlatformType).execute();
    }
}
