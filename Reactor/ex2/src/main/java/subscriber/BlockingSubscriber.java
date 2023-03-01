package subscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import utils.ExceptionUtils;
import common.Options;
import common.PrimeUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Flux} {@link Subscriber} that blocks the caller.
 */
public class BlockingSubscriber
       implements Subscriber<PrimeUtils.Result>,
                  Disposable {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Count the number of pending items.
     */
    private final AtomicInteger mPendingItemCount;

    /**
     * Subscription object.
     */
    private Subscription mSubscription;

    /**
     * Keeps track of whether we've been disposed.
     */
    private boolean mIsDisposed;

    /**
     * Barrier synchronizer that a calling thread can use to wait
     * until the subscriber completes all its processing.
     */
    private CountDownLatch mLatch;

    /**
     * Constructor initializes the field.
     */
    public BlockingSubscriber(AtomicInteger pendingItemCount) {
        // Initially false.
        mIsDisposed = false;

        // Store a reference to this count.
        mPendingItemCount = pendingItemCount;
    }

    /**
     * Hook method called when this subscriber is first subscribed.
     * It sets the initial request size.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        // Create a countdown latch that causes the main thread to
        // block until all flux processing is done.
        mLatch = new CountDownLatch(1);

        // Disable backpressure and inform the Publisher to
        // send requests as quickly as possible.
        subscription.request(Integer.MAX_VALUE);
    }

    /**
     * Hook method called when next item arrives.  It prints the
     * results of prime # checking and updates the next request size.
     */
    @Override
    public void onNext(PrimeUtils.Result result) {
        // Print the results of prime number checking
        if (result.mSmallestFactor != 0) {
            Options.debug(TAG, result.mPrimeCandidate
                          + " is not prime with smallest factor "
                          + result.mSmallestFactor);
        } else {
            Options.debug(TAG, result.mPrimeCandidate
                          + " is prime");
        }

        // Store the current pending item count. 
        int pendingItems = mPendingItemCount.decrementAndGet();

        Options.debug(TAG, "subscriber pending items: " + pendingItems);
    }

    /**
     * Hook method called to handle an error by printing the
     * exception.
     */
    @Override
    public void onError(Throwable t) { 
        Options.print("failure " + t); 

        // Release the latch since we're done.
        mLatch.countDown();
    }

    /**
     * Hook method that's called when all integers have been
     * processed.
     */
    @Override
    public void onComplete() {
        Options.print("completed");

        // Release the latch since we're done.
        mLatch.countDown();
    }

    /**
     * Hook method called when this subscriber is disposed.
     */
    @Override
    public void dispose() {
        mIsDisposed = true;
    }

    /**
     * @return True if this subscriber has been disposed, else false.
     */
    @Override
    public boolean isDisposed() {
        return mIsDisposed;
    }

    /**
     * Block caller until the latch is released.
     */
    public void await() {
        // Block caller until the latch is released.
        ExceptionUtils.rethrowRunnable(mLatch::await);
     }
}

