package vandy.mooc.pingpong.presenter;

import android.util.Log;

import java.util.List;

/**
 * This class starts two threads, Ping and Pong, that alternate
 * printing "Ping" and "Pong", respectively, on the display.
 */
public class PlayPingPongThread 
       extends Thread {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
            getClass().getSimpleName();

    /**
     * Reference to the Presenter layer.
     */
    private PingPongPresenter mPresenter;

    /**
     * List of threads used to play ping/pong.
     */
    private List<Thread> mPingPongThreads;

    /**
     * Constructor initializes the fields.
     */
    public PlayPingPongThread(PingPongPresenter presenter,
                              List<Thread> pingPongThreads) {
        // Store the presenter.
        mPresenter = presenter;
        
        // Store the list of ping/pong threads.
        mPingPongThreads = pingPongThreads;
    }

    /**
     * Override this method to ensure the PingPongThreads are
     * interrupted.
     */
    @Override
    public void interrupt() {
        // Interrupt the threads playing ping/pong.
        mPingPongThreads.forEach(Thread::interrupt);

        // Interrupt this thread.
        super.interrupt();
    }

    /**
     * Start running the ping/pong algorithm.
     */
    public void run() {
        // Let the user know we're starting. 
        mPresenter.println("Ready...Set...Go!");

        // Start ping and pong threads, which calls their run()
        // methods.
        mPingPongThreads.forEach(Thread::start);

        // Barrier synchronization to wait for all work to be done
        // before exiting play().
        try {
            for (Thread thread : mPingPongThreads) 
                thread.join();
        } catch(InterruptedException e) {
            Log.d(TAG,
                  "thread "
                  + Thread.currentThread()
                  + " interrupted");
        }

        // Let the user know we're done.
        mPresenter.println("Done!");
    }
}
