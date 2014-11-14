package example.pingpong;

/** 
 * @class PlatformStrategy
 *
 * @brief Provides methods that define a platform-independent API for
 *        output data to the display and synchronizing on thread
 *        completion in the ping/pong game.  This class is a singleton
 *        that also plays the role of the "Strategy" in the Strategy
 *        pattern.  Each platform (e.g., Android, command-line, etc.)
 *        subclasses from this singleton.
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
     * Do any initialization needed to start a new game. 
     */
    public abstract void begin();

    /** 
     * Print the outputString to the display. 
     */
    public abstract void print(String outputString);

    /** 
     * Indicate that a game thread has finished running. 
     */
    public abstract void done();

    /** 
     * Barrier that waits for all the game threads to finish. 
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
