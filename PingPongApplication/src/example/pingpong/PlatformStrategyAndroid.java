package example.pingpong;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import android.util.Log;

/**
 * @class PlatformStrategyAndroid
 * 
 * @brief Provides methods that define a platform-independent API for
 *        output data to Android UI thread and synchronizing on thread
 *        completion in the ping/pong game. It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern.
 */
public class PlatformStrategyAndroid extends PlatformStrategy {
    /**
     * Latch to decrement each time a thread exits to control when the
     * play() method returns.
     */
    private CountDownLatch mLatch = null;

    /** 
     * Define a WeakReference to avoid memory leaks. 
     */
    private final WeakReference<MainActivity> mOuterClass;

    /**
     * Constructor initializes the data member.
     */
    public PlatformStrategyAndroid(final Object output) {
        /** The current activity window (succinct or verbose). */
        mOuterClass = new WeakReference<MainActivity>
            ((MainActivity) output);
    }

    /**
     * Do any initialization needed to start a new game. 
     */
    public void begin() {
        /** (Re)initialize the CountDownLatch. */
        mLatch = new CountDownLatch(NUMBER_OF_THREADS);
    }

    /** 
     * Print the outputString to the display.
     */
    public void print(final String outputString) {
        /**
         * Create a Runnable that's ultimately posted to the UI looper
         * thread via another Thread that blocks for 0.5 seconds to
         * let the user see what's going on.
         */

        final MainActivity output = mOuterClass.get();
            
        if (output == null)
            return;
        try {
            // Use the PingPongInterface to make it a "1 liner" and
            // eliminates need for temp TextView variables, etc.
            output.print(outputString + "\n");
        } catch (NullPointerException ex) {
            errorLog("PlatformStrategyAndroid",
                     "print Failed b/c of null Activity");
        }
    }

    /** 
     * Indicate that a game thread has finished running. 
     */
    public void done() {
        final MainActivity output =
            mOuterClass.get();
            
        if (output == null)
            return;

        try {
            // Forward to the done() method, which posts a Runnable
            // whose run() method calls mLatch.countDown() on the UI
            // Thread after all other processing is complete.
            output.done(mLatch);
        } catch (NullPointerException ex) {
            errorLog("PlatformStrategyAndroid",
                     "print Failed b/c of null Activity");
        }
    }

    /** 
     * Barrier that waits for all the game threads to finish. 
     */
    public void awaitDone() {
        try {
            // Wait for the value of the CountDownLatch to reach 0.
            mLatch.await();
        } catch(java.lang.InterruptedException e) {
            errorLog( "PlatformStrategyAndroid", e.getMessage());
        }
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile,
                         String errorMessage) {
        Log.e(javaFile, errorMessage);
    }
}
