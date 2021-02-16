package example.pingpong.threads;

import example.pingpong.platform.PlatformStrategy;

/**
 * This class implements the core ping/pong algorithm, but defers the
 * synchronization aspects to subclasses. It plays the role of the
 * "Abstract Class" in the Template Method pattern.
 */
public abstract class PingPongThread 
       extends Thread {
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
     * Constructor initializes the various fields.
     */
    public PingPongThread(String stringToPrint,
                          int maxIterations) {
        mStringToPrint = stringToPrint;
        mMaxIterations = maxIterations;
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
        for (int loopsDone = 1; loopsDone <= mMaxIterations; ++loopsDone) {
            // Perform the template method protocol for printing a
            // "ping" or a "pong" on the display. The acquire() and
            // release() hook methods that control the scheduling of
            // the threads are deferred to subclasses.
            acquire();
            PlatformStrategy.instance().print(mStringToPrint
                                    + "(" 
                                    + loopsDone 
                                    + ")");
            release();
        }

        // Indicate that this thread is done playing ping/pong.
        PlatformStrategy.instance().done();

        // Exit the thread when the loop is done.
    }
}

