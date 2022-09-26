package example.pingpong.platform;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import android.util.Log;

import example.pingpong.MainActivity;

/**
 * @class PlatformStrategyAndroid
 * 
 * @brief Implements a platform-independent API for outputting data to
 *        Android UI thread and synchronizing on thread completion in
 *        the ping/pong application. It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern.
 */
public class PlatformStrategyAndroid extends PlatformStrategy {
    /**
     * An exit barrier that's decremented each time a thread exits,
     * which control when the PlayPingPong run() hook method returns.
     */
    private CountDownLatch mExitBarrier = null;

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
     * Do any initialization needed to start a new running the
     * ping/pong algorithm.
     */
    public void begin() {
        /** (Re)initialize the CountDownLatch. */
        mExitBarrier = new CountDownLatch(NUMBER_OF_THREADS);
    }

    /** 
     * Barrier that waits for all the Threads to finish. 
     */
    public void awaitDone() {
        try {
            // Wait until the CountDownLatch reaches 0.
            mExitBarrier.await();
        } catch(java.lang.InterruptedException e) {
            errorLog("PlatformStrategyAndroid",
                     e.getMessage());
        }
    }

    /** 
     * Output the string to the Android display managed by the UI Thread.
     */
    public void print(final String outputString) {
        final MainActivity output = mOuterClass.get();
            
        if (output == null)
            return;
        try {
            // Calls the MainActivity.print() method, which create a
            // Runnable that's ultimately posted to the UI Thread via
            // another Thread that sleeps for 0.5 seconds to let the
            // user see what's going on.
            output.print(outputString + "\n");
        } catch (NullPointerException ex) {
            errorLog("PlatformStrategyAndroid",
                     "print Failed b/c of null Activity");
        }
    }

    /** 
     * Indicate that a Thread has finished running. 
     */
    public void done() {
        final MainActivity output =
            mOuterClass.get();
            
        if (output == null)
            return;

        try {
            // Forward to the MainActivity.done() method, which
            // ultimately posts a Runnable on the UI Thread. This
            // Runnable's run() method calls mExitBarrier.countDown() in the
            // context of the UI Thread after all other processing is
            // complete.
            output.done(mExitBarrier);
        } catch (NullPointerException ex) {
            errorLog("PlatformStrategyAndroid",
                     "print Failed b/c of null Activity");
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
