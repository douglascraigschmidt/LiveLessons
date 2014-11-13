package example.pingpong;

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
        mMine = mine;
        mOther = other;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    void acquire() {
        // Block until we receive the pingpong ball.

        mOther.awaitBall();
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    void release() {
        // Hit the pingball back.

        mMine.returnBall();
    }

}

