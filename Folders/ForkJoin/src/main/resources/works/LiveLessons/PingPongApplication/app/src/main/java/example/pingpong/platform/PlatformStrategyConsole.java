package example.pingpong.platform;

import java.util.concurrent.CountDownLatch;
import java.io.PrintStream;

/**
 * @class PlatformStrategyConsole
 *
 * @brief Implements a platform-independent API for outputting data to
 *        the console window and synchronizing on thread completion in
 *        the ping/pong application.  It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern.
 */
public class PlatformStrategyConsole extends PlatformStrategy {
    /**
     * An exit barrier that's decremented each time a thread exits,
     * which control when the PlayPingPong run() hook method returns.
     */
    private CountDownLatch mExitBarrier = null;

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
     * Perform any initialization needed to start running the
     * ping/pong algorithm.
     */
    public void begin() {
        mExitBarrier = new CountDownLatch(NUMBER_OF_THREADS);
    }

    /** 
     * Print the outputString to the display. 
     */
    public void print(String outputString) {
        // Print to the console window.
        mOutput.println(outputString);
    }

    /** 
     * Indicate that a Thread has finished running.
     */
    public void done() {
        // Decrement the CountDownLatch by one.
        mExitBarrier.countDown();
    }
    
    /** 
     * Barrier that waits for all the Threads to finish.
     */
    public void awaitDone() {
        try {
            // Wait until the CountDownLatch reaches 0.
            mExitBarrier.await();
        } catch(java.lang.InterruptedException e) {
            errorLog("PlatformStrategyConsole",
                     e.getMessage());
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

