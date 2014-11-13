package edu.vuum.mocca;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class BallPaddle
 * 
 * @brief This class uses Java Condition and ReentrantLock to
 *        implement a blocking pingpong paddle abstraction.
 */
public class BallPaddle {
    /** Condition used to wait for the Pingpong ball. */
    // TODO - You fill in here.
    private final Condition mBall;

    /** Lock used along with the Condition. */
    // TODO - You fill in here.
    private final Lock mLock;

    /** Do we have the ball or not. */
    // TODO - You fill in here.
    private boolean mHaveBall;

    /**
     * Constructor initializes data members.
     */
    public BallPaddle(boolean haveBall) {
        // TODO - You fill in here.
        mLock = new ReentrantLock();
        mBall = mLock.newCondition();
        mHaveBall = haveBall;
    }

    /**
     * Waits until the other BallPaddle hits the ball to us.
     */
    public void awaitBall() {
        // TODO - You fill in here.
        mLock.lock();
        try {
            // Wait until we've been hit the ball.
            while (mHaveBall == false)
                mBall.awaitUninterruptibly();
            mHaveBall = false;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Returns the ball to the other BallPaddle.
     */
    public void returnBall() {
        // TODO - You fill in here.
        mLock.lock();
        try {
            mHaveBall = true;
            // Signal that the ball is now available.
            mBall.signal();
        } finally {
            mLock.unlock();
        }
    }
}

