package managedblockers;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a ManagedBlocker that potentially blocks until a
 * {@link ReentrantLock} is available.
 */
public class ManagedLocker
       implements ForkJoinPool.ManagedBlocker {
    /**
     * The {@link ReentrantLock}.
     */ 
    final Lock mLock;

    /**
     * Keeps track of whether the lock was obtained.
     */
    boolean mHasLock = false;

    /**
     * Constructor initializes the field.
     */
    public ManagedLocker(Lock lock) {
        mLock = lock; 
    }

    /**
     * Tries to obtain the lock if it's immediately available, but
     * doesn't block if it's not available.
     */
    public boolean isReleasable() {
        return mHasLock || (mHasLock = mLock.tryLock()); 
    }

    /**
     * Block until the lock is available and lock it.
     */
    public boolean block() {
        if (!mHasLock) 
            mLock.lock();
        return true;  
    } 
}

