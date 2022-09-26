package vandy.mooc.pingpong.presenter;

import android.util.Log;

/**
 * This class implements the core ping/pong algorithm, but defers the
 * synchronization aspects to subclasses. It plays the role of the
 * "Abstract Class" in the Template Method pattern.
 */
public abstract class PingPongThread 
                extends Thread {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * Number of iterations to ping/pong.
     */
    private final int mMaxIterations;

    /**
     * Data member that indicates the string to print (typically a
     * "ping" or a "pong").
     */
    private final String mStringToPrint;

    /**
     * Store a reference to the PingPongPresenter.
     */
    private final PingPongPresenter mPresenter;

    /**
     * Constructor initializes the fields.
     */
    public PingPongThread(PingPongPresenter presenter,
                          String stringToPrint,
                          int maxIterations) {
        mPresenter = presenter;
        mStringToPrint = stringToPrint;
        mMaxIterations = maxIterations;
    }

    /**
     * Get the presenter.
     */
    private PingPongPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * Abstract hook methods that determine the ping/pong scheduling
     * protocol in the run() template method.
     */
    protected abstract void acquire();
    protected abstract void release();

    /**
     * This method runs in a separate thread of control and implements
     * the core ping/pong algorithm. It plays the role of the
     * "template method" in the Template Method pattern.
     */
    public void run() {
        for (int loopsDone = 1; 
             loopsDone <= mMaxIterations;
             ++loopsDone) {

            // Check to see if we've been interrupted and break out of
            // the loop if so.
            if (Thread.interrupted()) {
                Log.d(TAG,
                      "thread "
                      + Thread.currentThread()
                      + " interrupted");
                break;
            }

            // Perform the template method protocol for printing a
            // "ping" or a "pong" on the display. The acquire() and
            // release() hook methods that control the scheduling of
            // the threads are deferred to subclasses.
            acquire();

            getPresenter().println(mStringToPrint 
                                   + "(" 
                                   + loopsDone 
                                   + ")");
            release();
        }

        // Exit the thread when the loop is done.
    }
}

