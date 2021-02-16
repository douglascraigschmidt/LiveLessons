package example.pingpong.platform;

/** 
 * @class PlatformStrategy
 *
 * @brief Provides methods that define a platform-independent
 *        mechanism for outputting data to the display and
 *        synchronizing on Thread completion in the ping/pong
 *        application.  This class is a singleton that also plays the
 *        role of the "Strategy" in the Strategy pattern and the
 *        Product in the Factory Method pattern.  Both the
 *        PlatformStrategyConsole and PlatformStrategyAndroid
 *        subclasses extend this class.
 */
public abstract class PlatformStrategy
{
    /** 
     * Number of threads used to play ping-pong. 
     */
    public static final int NUMBER_OF_THREADS = 2;

    /** 
     * The singleton @a PlatformStrategy instance. 
     */
    private static PlatformStrategy sUniqueInstance = null;

    /** 
     * Method to return the one and only singleton instance. 
     */
    public static PlatformStrategy instance() {
        return sUniqueInstance;
    }

    /** 
     * Method that sets a new PlatformStrategy singleton and returns the one
     * and only singleton instance.
     */
    public static PlatformStrategy instance(PlatformStrategy platform) {
        return sUniqueInstance = platform;
    }

    /** 
     * Perform any initialization needed to start running the
     * ping/pong algorithm.
     */
    public abstract void begin();

    /** 
     * Outputs the string parameter to the display. 
     */
    public abstract void print(String outputString);

    /** 
     * Indicates a Thread has finished running the ping/pong
     * algorithm.
     */
    public abstract void done();

    /** 
     * Barrier that waits for all Threads to finish running the
     * ping/pong algorithm.
     */
    public abstract void awaitDone();

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public abstract void errorLog(String javaFile,
                                  String errorMessage);

    /**
     * Make the constructor protected to ensure singleton access.
     */
    protected PlatformStrategy() {}
}
