package edu.vuum.mocca;

/**
 * @class PingPongThreadMonObj
 * 
 * @brief This class uses Binary Semaphores (implelemented as Java
 *        built-in monitor objects) to implement the acquire() and
 *        release() hook methods that schedule the ping/pong
 *        algorithm. It plays the role of the "Concrete Class" in the
 *        Template Method pattern.
 */
class PingPongThreadMonObj extends PingPongThread {
    /**
     * Max number of ping pong semaphores.
     */
    private final static int MAX_PING_PONG_SEMAS = 2;

    /**
     * Semaphores that schedule the ping/pong algorithm.
     */
    private final BinarySemaphore mSemas[] =
        new BinarySemaphore[MAX_PING_PONG_SEMAS];

    /**
     * Consts to distinguish between ping and pong BinarySemaphores.
     */
    private final static int FIRST_SEMA = 0;
    private final static int SECOND_SEMA = 1;

    PingPongThreadMonObj(String stringToPrint,
                         BinarySemaphore firstSema,
                         BinarySemaphore secondSema,
                         boolean isOwner,
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

        mSemas[FIRST_SEMA].acquire();
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
