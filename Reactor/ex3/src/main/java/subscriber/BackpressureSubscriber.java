package subscriber;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import utils.ExceptionUtils;
import common.Options;
import utils.PrimeUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Flux} {@link CoreSubscriber} that implements backpressure.
 */
public class BackpressureSubscriber
    implements CoreSubscriber<PrimeUtils.Result>,
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
     * Subscription used to control backpressure.
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
     * Request size.
     */
    private final int mREQUEST_SIZE = 10;

    /**
     * Number of items processed since the last request was sent.
     */
    private int mItemsProcessedSinceLastRequest;

    /**
     * Constructor initializes the field.
     */
    public BackpressureSubscriber(AtomicInteger pendingItemCount) {
        // Initially false.
        mIsDisposed = false;

        // Store a reference to this count.
        mPendingItemCount = pendingItemCount;

        // Keep track of number of items processed since the last
        // request was made upstream.
        mItemsProcessedSinceLastRequest = 0;
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

        // Store the subscription for later use.
        mSubscription = subscription;

        // Set the initial request size.
        mSubscription.request(mREQUEST_SIZE);
    }

    /**
     * Hook method called when next item arrives.  It prints the
     * results of prime # checking and updates the next request size.
     */
    @Override
    public void onNext(PrimeUtils.Result result) {
        PrimeUtils.printResult(result);

        // Store the current pending item count. 
        int pendingItems = mPendingItemCount.decrementAndGet();

        Options.debug(TAG, "subscriber pending items: "
                      + pendingItems);

        // Check to see if we've consumed 70% our window of items.
        if (++mItemsProcessedSinceLastRequest == mREQUEST_SIZE) {
            Options.debug(TAG,
                "subscriber requesting next tranche of items");

            // Request next tranche of items.
            mSubscription.request(mREQUEST_SIZE);

            // Reset the counter.
            mItemsProcessedSinceLastRequest = 0;
        }
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

