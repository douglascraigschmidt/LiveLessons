package example.pingpong;

import java.util.concurrent.CountDownLatch;
import java.io.PrintStream;

/**
 * @class PlatformStrategyConsole
 *
 * @brief Provides methods that define a platform-independent API for
 *        output data to the console window and synchronizing on
 *        thread completion in the ping/pong game.  It plays the role
 *        of the "Concrete Strategy" in the Strategy pattern.
 */
public class PlatformStrategyConsole extends PlatformStrategy {
    /**
     * Latch to decrement each time a thread exits to control when the
     * play() method returns.
     */
    private CountDownLatch mLatch = null;

    /**
     * Contains information for printing output to the console window.
     */
    private final PrintStream mOutput;

    /** 
     * Constructor initializes the data member.
     */
    public PlatformStrategyConsole(Object output) {
        mOutput = (PrintStream) output;
    }
	
    /** 
     * Do any initialization needed to start a new game. 
     */
    public void begin() {
        mLatch = new CountDownLatch(NUMBER_OF_THREADS);
    }

    /** 
     * Print the outputString to the display. 
     */
    public void print(String outputString) {
        // Print to the console window.
        mOutput.println(outputString);
    }

    /** 
     * Indicate that a game thread has finished running. 
     */
    public void done() {
        // Decrement the CountDownLatcy by one.
        mLatch.countDown();
    }
    
    /** 
     * Barrier that waits for all the game threads to finish. 
     */
    public void awaitDone() {
        try {
            // Wait until the CountDownLatch reaches 0.
            mLatch.await();
        } catch(java.lang.InterruptedException e) {
        }
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile, String errorMessage) {
        mOutput.println(javaFile 
                        + " " 
                        + errorMessage);
    }
}

