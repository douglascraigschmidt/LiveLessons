package edu.vuum.mocca;

import java.util.HashMap;

/**
 * @class PlatformStrategyFactory
 * 
 * @brief This class is a Factory that uses the Command pattern to
 *        create the designated @a PlatformStrategy implementation
 *        (e.g., either Android or Java console application) at
 *        runtime.
 */
public class PlatformStrategyFactory {
    /**
     * This interface uses the Command pattern to create @a
     * PlatformStrategy implementations at runtime.
     */
    private static interface IPlatformStrategyFactoryStrategy {
        public PlatformStrategy execute();
    }

    /**
     * Enumeration distinguishing platforms Android from plain ol' Java.
     */
    public enum PlatformType {
        ANDROID, PLAIN_JAVA
    };
    
    /**
     * Keep track of the type of platform.
     */
    private static PlatformType mPlatformType;

    /**
     * HashMap used to map strings containing the Java platform names
     * and dispatch the execute() method of the associated @a
     * PlatformStrategy implementation.
     */
    private HashMap<PlatformType, IPlatformStrategyFactoryStrategy> mPlatformStrategyMap =
        new HashMap<PlatformType, IPlatformStrategyFactoryStrategy>();

    /**
     * Ctor that stores the objects that perform output for a particular
     * platform, such as ConsolePlatformStrategy or the AndroidPlatformStrategy.
     */
    public PlatformStrategyFactory(final Object output) {
        // Cache this value in the constructor since it won't change
        // at runtime.
        mPlatformType = platformName().indexOf("Android") >= 0
            ? PlatformType.ANDROID
            : PlatformType.PLAIN_JAVA;

        /**
         * The "The Android Project" string maps to a command object that
         * creates an @a AndroidPlatformStrategy implementation.
         */
        mPlatformStrategyMap.put(PlatformType.ANDROID,
                                 new IPlatformStrategyFactoryStrategy() {
                                     /**
                                      * Creates the AndroidPlatformStrategy.
                                      */
                                     public PlatformStrategy execute() {
                                         return new AndroidPlatformStrategy(output);
                                     }
                                 });

        /**
         * The "Sun Microsystems Inc." string maps to a command object
         * that creates an @a ConsolePlatformStrategy implementation.
         */
        mPlatformStrategyMap.put(PlatformType.PLAIN_JAVA,
                                 /*
                                  * Creates the AndroidPlatformStrategy.
                                  */
                                 new IPlatformStrategyFactoryStrategy() {
                                     public PlatformStrategy execute() {
                                         return new ConsolePlatformStrategy(output);
                                     }
                                 });
    }

    /**
     * Returns the name of the platform in a string. e.g., Android or a JVM.
     */
    public static String platformName() {
        return System.getProperty("java.specification.vendor");
    }

    /**
     * Returns the type of the platformm e.g. Android or a JVM.
     */
    public static PlatformType platformType() {
        return mPlatformType;
    }

    /**
     * Create a new @a PlatformStrategy object based on underlying Java
     * platform.
     */
    public PlatformStrategy makePlatformStrategy() {
        return mPlatformStrategyMap.get(platformType()).execute();
    }
}
