package example.pingpong;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class PingPongThreadCond
 * 
 * @brief This class uses Java Conditions and ConditionObjects to
 *        implement the acquire() and release() hook methods that
 *        schedule the ping/pong algorithm. It plays the role of the
 *        "Concrete Class" in the Template Method pattern.
 */
class PingPongThreadCond extends PingPongThread {
    /**
     * Max number of ping pong conditions.
     */
    private final static int MAX_PING_PONG_CONDS = 2;

    /**
     * Conditions that are used to schedule the ping/pong algorithm.
     */
    private final Condition mConds[] =
        new Condition[MAX_PING_PONG_CONDS];

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
     * Consts to distinguish between ping and pong conditions.
     */
    private final static int FIRST_COND = 0;
    private final static int SECOND_COND = 1;

    PingPongThreadCond(String stringToPrint,
                       ReentrantLock lock,
                       Condition firstCond,
                       Condition secondCond,
                       boolean isOwner,
                       int maxIterations) {
        super(stringToPrint, maxIterations);
        mLock = lock;
        mConds[FIRST_COND] = firstCond;
        mConds[SECOND_COND] = secondCond;
        if (isOwner)
            mTurnOwner = this.getId();
    }

    public void setOtherThreadId(long otherThreadId) {
        this.mOtherThreadId = otherThreadId;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    void acquire() {
        mLock.lock();

        while (mTurnOwner != this.getId()) {
            mConds[FIRST_COND].awaitUninterruptibly();
        }

        mLock.unlock();
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    void release() {
        mLock.lock();

        mTurnOwner = mOtherThreadId;
        mConds[SECOND_COND].signal();
        mLock.unlock();
    }
}

