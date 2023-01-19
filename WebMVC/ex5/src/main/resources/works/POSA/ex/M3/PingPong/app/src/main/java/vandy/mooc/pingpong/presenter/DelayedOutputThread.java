package vandy.mooc.pingpong.presenter;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class delays printing output to the display by 0.5 seconds so
 * the user can see what's happening on the screen.
 */
public class DelayedOutputThread
       extends Thread {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * Queues Strings awaiting to be output on the display.
     */
    private LinkedBlockingQueue<String> mQueue =
        new LinkedBlockingQueue<>();

    /**
     * Reference back to the Presenter layer.
     */
    private final PingPongPresenter mPresenter;

    /**
     * Constructor initializes the field.
     */
    public DelayedOutputThread(PingPongPresenter presenter) {
        mPresenter = presenter;
    }

    /**
     * Runs in the background dequeueing and printing commands every
     * 0.5 seconds.
     */
    @Override
    public void run() {
        try {
            for (String output;
                 (output = mQueue.take()) != null;
                 ) {

                // Print on the UI thread.
                mPresenter.printOnUiThread(output);

                // Wait 0.5 seconds (500 milliseconds) before
                // handling the next message.
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Log.d(TAG,
                  "thread "
                  + Thread.currentThread()
                  + " interrupted");
            // Remove all the entries from the queue.
            mQueue.clear();
        }
    }

    /**
     * Put the @a output string in the queue without blocking.
     */
    public void put(String output) {
        try {
            mQueue.put(output);
        } catch (InterruptedException e) {
            Log.d(TAG,
                  "thread "
                  + Thread.currentThread()
                  + " interrupted");
        }
    }
}

        	
