package example.pingpong;

import java.util.concurrent.CountDownLatch;
import java.io.PrintStream;

/**
 * @class ConsolePlatformStrategy
 *
 * @brief Provides methods that define a platform-independent API for
 *        output data to the console window and synchronizing on
 *        thread completion in the ping/pong game.  It plays the role
 *        of the "Concrete Strategy" in the Strategy pattern.
 */
public class ConsolePlatformStrategy extends PlatformStrategy
{
    /**
     * Latch to decrement each time a thread exits to control when the
     * play() method returns.
     */
    private CountDownLatch mLatch = null;

    /** Contains information for outputting to console window. */
    private final PrintStream mOutput;

    /** Ctor. */
    public ConsolePlatformStrategy(Object output) 
    {
        mOutput = (PrintStream) output;
    }
	
    /** Do any initialization needed to start a new game. */
    public void begin()
    {
        mLatch = new CountDownLatch(NUMBER_OF_THREADS);
    }

    /** Print the outputString to the display. */
    public void print(String outputString)
    {
        /** Print to the console window. */
        mOutput.println(outputString);
    }

    /** Indicate that a game thread has finished running. */
    public void done()
    {
        mLatch.countDown();
    }
    
    /** Barrier that waits for all the game threads to finish. */
    public void awaitDone()
    {
        try {
            mLatch.await();
        } catch(java.lang.InterruptedException e) {
        }
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile, String errorMessage) 
    {
        mOutput.println(javaFile + " " + errorMessage);
    }
}

