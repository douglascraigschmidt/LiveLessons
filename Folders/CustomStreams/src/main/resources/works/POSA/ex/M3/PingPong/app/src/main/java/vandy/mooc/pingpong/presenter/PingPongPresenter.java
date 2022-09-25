package vandy.mooc.pingpong.presenter;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import vandy.mooc.pingpong.model.PingPongModel;
import vandy.mooc.pingpong.view.MainActivity;

/**
 * This class defines the entry point into the Presenter layer.
 */
public class PingPongPresenter {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * Define a WeakReference to the View layer to avoid memory leaks
     * during runtime orientation changes.
     */
    private WeakReference<MainActivity> mView;

    /**
     * A reference to the Model layer.
     */
    private PingPongModel mModel;

    /**
     * Maximum number of Presenter threads.
     */
    private final static int sMAX_PRESENTER_THREADS = 2;

    /**
     * Constants used to distinguish between the Presenter threads.
     */
    private final static int sDELAYED_OUTPUT_THREAD = 0;
    private final static int sPLAY_PING_PONG_THREAD = 1;

    /** 
     * A list of threads.
     */
    private List<Thread> mThreads = 
        Arrays.asList(new Thread[sMAX_PRESENTER_THREADS]);

    /** 
     * Variables used to track state of the ping/pong game.
     */
    public enum Gamestate {
        PLAY,
        RESET
    }

    /**
     * Keep track of the current gamestate.
     */
    private Gamestate mGamestate = Gamestate.PLAY;

    /**
     * Get the current gamestate.
     */
    public Gamestate getGamestate() {
        return mGamestate;
    }

    /**
     * Set the current gamestate.
     */
    public void setGamestate(Gamestate gamestate) {
         mGamestate = gamestate;
    }

    /**
     * Constructor initializes the fields.
     */
    public PingPongPresenter(MainActivity activity) {
        // Store the reference to the View layer.
        mView = new WeakReference<>(activity);

        // Store the reference to the Model layer.
        mModel = new PingPongModel();
    }

    /**
     * Hook method dispatched to reinitialize the Presenter layer
     * after a runtime configuration change.
     *
     * @param view         
     *          The currently active activity view.
     */
    public void onConfigurationChange(MainActivity view) {
        Log.d(TAG,
              "onConfigurationChange() called");

        // Reset the WeakReference.
        mView = new WeakReference<>(view);
    }

    /**
     * Beging playing ping/pong by creating and starting the threads.
     */
    public void play(int maxIterations,
                     String syncMechanism) {
        // Create and start a thread that runs calls to print() on the
        // UI thread after a short delay.
        mThreads.set(sDELAYED_OUTPUT_THREAD,
                     new DelayedOutputThread(this));

        // Create and start a thread that plays ping-pong.
        mThreads.set(sPLAY_PING_PONG_THREAD,
                     new PlayPingPongThread
                     (this,
                      // Create ping and pong threads with the given
                      // max iteration and synchronization mechanism.
                      getModel().makePingPongThreads(this,
                                                     maxIterations,
                                                     syncMechanism)));

        // Start the threads.
        mThreads.forEach(Thread::start);
    }

    /**
     * Reset the Presenter layer by shutting down all the threads.
     */
    public void reset() {
        try {
            // Interrupt, join, and reset all the threads.
            for (int i = 0;
                 i < mThreads.size();
                 ++i) {
                // Interrupt the thread.
                mThreads.get(i).interrupt();
                
                // Wait for the thread to finish.
                mThreads.get(i).join();

                // Release the thread.
                mThreads.set(i, null);
            }
        } catch (InterruptedException e) {
            Log.d(TAG,
                  "thread "
                  + Thread.currentThread()
                  + " interrupted");
        }
    }

    /**
     * Prints the output string to the text log on screen after
     * appending a "\n". If the string contains "ping"
     * (case-insensitive) then a large Ping!  will be shown on screen
     * with a certain color. Likewise, if the string contains "pong"
     * then a large Pong!  will be shown on the screen with a
     * different color.
     * 
     * A call to this function will not block. However, the code to
     * display this output will be posted to a thread in such a way
     * that any changes to the UI will be spaced out by 0.5 seconds so
     * the user has an appropriate amount of time to appreciate the
     * ping'ing and the pong'ing that is happening.
     */
    public void println(String output) {
        // Enqueue a string that prints the output to the UI with a
        // 0.5 second delay between displaying the output.
        ((DelayedOutputThread) mThreads
                .get(sDELAYED_OUTPUT_THREAD))
                .put(output + "\n");
    }

    /**
     * Forwards to the View layer to print the @a output on the UI
     * thread.
     */
    public void printOnUiThread(String output) {
        try {
            getView().printOnUiThread(output);
        } catch (NullPointerException e) {
            Log.d(TAG,
                  "view has gone away!");
        }
    }

    /**
     * Get the reference to the Model layer.
     */
    public PingPongModel getModel() {
        return mModel;
    }

    /**
     * Get the reference to the View layer.
     */
    public MainActivity getView() {
        return mView.get();
    }
}
