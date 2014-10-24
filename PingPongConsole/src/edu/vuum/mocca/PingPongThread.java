package edu.vuum.mocca;

/**
 * @Brief PingPongThread
 *
 * @class This class implements the concurrent ping/pong algorithm.
 */
public class PingPongThread extends Thread {
    /**
     * Data member that indicates the string to print (typically a
     * "ping" or a "pong").
     */
    protected String mStringToPrint;

    /**
     * Number of iterations to ping/pong.
     */
    protected final int mMaxIterations;

    /**
     * Semaphores that schedule the ping/pong algorithm.
     */
    private BinarySemaphore mFirstSema;
    private BinarySemaphore mSecondSema;

    /** 
     * Constructor initializes the various fields.
     */
    PingPongThread(String stringToPrint,
                   BinarySemaphore firstSema,
                   BinarySemaphore secondSema,
                   int maxIterations) {
        mStringToPrint = stringToPrint;
        mFirstSema = firstSema;
        mSecondSema = secondSema;
        mMaxIterations = maxIterations;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    void acquire() {
        mFirstSema.acquire();
    }

    /**
     * Hook method for ping/pong release.
     */
    void release() {
        mSecondSema.release();
    }

    /**
     * This method runs in a separate thread of control and implements
     * the core ping/pong algorithm.  It plays the role of the
     * "template method" in the Template Method pattern.
     */
    public void run() {
        for (int loopsDone = 1;
             loopsDone <= mMaxIterations;
             ++loopsDone) {
            // Perform the template method protocol for printing a
            // "ping" or a "pong" on the display.  Note that the
            // acquire() and release() hook methods that control the
            // scheduling of the threads are deferred to subclasses.
            acquire();
            System.out.println(mStringToPrint 
                               + "(" 
                               + loopsDone 
                               + ")");
            release();
        }

        // Exit the thread when the loop is done.
    }
}

