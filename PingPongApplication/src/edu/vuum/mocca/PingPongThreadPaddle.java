package edu.vuum.mocca;

 /**
  * @class PingPongThreadPaddle
  * 
  * @brief This class uses the PingPongPaddle class to implement the
  *        acquire() and release() hook methods that schedule the
  *        ping/pong algorithm.  It plays the role of the "Concrete
  *        Class" in the Template Method pattern.
  */
public class PingPongThreadPaddle extends PingPongThread {
    /**
     * These PingPongPaddle objects handle synchronization between
     * our thread and the other Thread.
     */
    // TODO - You fill in here.
    private final BallPaddle mMine;
    private final BallPaddle mOther;

    /**
     * Constructor initializes the various fields.
     */
    PingPongThreadPaddle(String stringToPrint,
                         BallPaddle mine,
                         BallPaddle other,
                         int maxIterations) {
        super(stringToPrint, maxIterations);
        // TODO - You fill in here.
        mMine = mine;
        mOther = other;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    void acquire() {
        // Block until we receive the pingpong ball.

        // TODO - You fill in here.
        mOther.awaitBall();
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    void release() {
        // Hit the pingball back.

        // TODO - You fill in here.
        mMine.returnBall();
    }

}

