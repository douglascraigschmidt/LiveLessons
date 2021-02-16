package utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

/**
 * Use a StampedLock to implement a non-reentrant lock.
 */
public class NonReentrantLock 
       implements Lock {
    /**
     * StampedLock is non-reentrant.
     */
    private final StampedLock mLock = new StampedLock();

    /**
     * Store the stamp to use for unlocking.
     */
    private long mStamp;

    @Override
    public void lock() {
        mStamp = mLock.writeLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        mStamp = mLock.writeLock();
    }

    @Override
    public boolean tryLock() {
        mStamp = mLock.tryWriteLock();
        return mStamp != 0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        mStamp = mLock.tryWriteLock(time, unit);
        return mStamp != 0;
    }

    @Override
    public void unlock() {
        mLock.unlockWrite(mStamp);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Condition newCondition() {
        return null;
    }
}
