package edu.vuum.mocca;

import java.util.concurrent.Semaphore;

/**
 * @class PingPongThreadSema
 *
 * @brief This class uses Java Semaphores to implement the acquire()
 *        and release() hook methods that schedule the ping/pong
 *        algorithm. It plays the role of the "Concrete Class" in the
 *        Template Method pattern.
 */
class PingPongThreadSema extends PingPongThread {
    /**
     * Max number of ping pong semaphores.
     */
    private final static int MAX_PING_PONG_SEMAS = 2;

    /**
     * Semaphores that schedule the ping/pong algorithm.
     */
    private final Semaphore mSemas[] =
        new Semaphore[MAX_PING_PONG_SEMAS];

    /**
     * Consts to distinguish between ping and pong Semaphores.
     */
    private final static int FIRST_SEMA = 0;
    private final static int SECOND_SEMA = 1;

    PingPongThreadSema(String stringToPrint, 
                       Semaphore firstSema,
                       Semaphore secondSema,
                       int maxIterations) {
        super(stringToPrint, maxIterations);
        mSemas[FIRST_SEMA] = firstSema;
        mSemas[SECOND_SEMA] = secondSema;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    void acquire() {
        // Block until we acquire the semaphore.
        mSemas[FIRST_SEMA].acquireUninterruptibly();
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    void release() {
        // Release the other semaphore.
        mSemas[SECOND_SEMA].release();
    }
}
