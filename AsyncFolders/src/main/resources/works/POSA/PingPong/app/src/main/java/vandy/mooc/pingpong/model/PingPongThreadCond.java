package vandy.mooc.pingpong.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import vandy.mooc.pingpong.presenter.PingPongPresenter;
import vandy.mooc.pingpong.presenter.PingPongThread;

/**
 * This class uses Java Conditions and ConditionObjects to implement
 * the acquire() and release() hook methods that synchronize the
 * ping/pong algorithm. It plays the role of the "Concrete Class" in
 * the Template Method pattern.
 */
class PingPongThreadCond 
      extends PingPongThread { 
    /**
     * Conditions that are used to schedule the ping/pong algorithm.
     */
    private final Condition mMine;
    private final Condition mOther;

    /**
     * Monitor lock.
     */
    private final ReentrantLock mLock;

    /**
     * Id for the other thread.
     */
    public long mOtherThreadId = 0;

    /**
     * Thread whose turn it currently is.
     */
    private static long mTurnOwner;

    /**
     * Constructor initializes the fields and superclass.
     */
    public PingPongThreadCond(PingPongPresenter presenter,
                              String stringToPrint,
                              ReentrantLock lock,
                              Condition mine,
                              Condition other,
                              boolean isOwner,
                              int maxIterations) {
        super(presenter, stringToPrint, maxIterations);
        mLock = lock;
        mMine = mine;
        mOther = other;
        if (isOwner)
            mTurnOwner = this.getId();
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    protected void acquire() {
        mLock.lock();

        try {
            // Wait until we're the turn owner.
            while (mTurnOwner != this.getId()) 
                mMine.awaitUninterruptibly();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    protected void release() {
        mLock.lock();

        try {
            // Make the other Thread the turn owner and signal it to
            // wake up.
            mTurnOwner = mOtherThreadId;
            mOther.signal();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Keep track of the ID of the other Thread.
     */
    public void setOtherThreadId(long otherThreadId) {
        mOtherThreadId = otherThreadId;
    }
}

