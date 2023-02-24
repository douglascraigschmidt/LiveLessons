package edu.vandy.pubsub.subscriber;

import edu.vandy.pubsub.common.Options;
import edu.vandy.pubsub.common.Result;
import edu.vandy.pubsub.utils.ExceptionUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;

import java.util.concurrent.CountDownLatch;

/**
 * A Flux subscriber that implements hybrid push/pull backpressure.
 */
public class HybridBackpressureSubscriber
       implements Subscriber<Result>,
                  Disposable {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Subscription used to control backpressure.
     */
    private Subscription mSubscription;

    /**
     * Keeps track of whether we've been disposed.
     */
    private boolean mIsDisposed;

    /**
     * Barrier synchronizer a calling thread can use to wait until the
     * subscriber has completed all its processing.
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
    public HybridBackpressureSubscriber() {
        // Initially false.
        mIsDisposed = false;

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
        mSubscription
            .request(nextRequestSize());
    }

    /**
     * @return The next request size for the publisher.
     */
    int nextRequestSize() {
        if (!Options.instance().backPressureEnabled())
            // Disable backpressure.
            return Integer.MAX_VALUE;
        else {
            // Request only this many items.
            return mREQUEST_SIZE;
        }
    }

    /**
     * Hook method called when next item arrives.  It prints the
     * results of prime # checking and updates the next request size.
     */
    @Override
    public void onNext(Result result) {
        // Print the results of prime number checking
        if (result.smallestFactor != 0) {
            Options.debug(TAG, result.primeCandidate
                          + " is not prime with smallest factor "
                          + result.smallestFactor);
        } else {
            Options.debug(TAG, result.primeCandidate
            + " is prime");
        }

        if (Options.instance().backPressureEnabled()) {
            // Compute 70% of mREQUEST_SIZE.
            int seventyPercent = (int) (mREQUEST_SIZE * (70.0f / 100.0f));

            // Check to see if we've consumed 70% our window of items.
            if (++mItemsProcessedSinceLastRequest == seventyPercent) {
                Options.debug(TAG, "subscriber requesting next tranche of items");

                // Request next tranche of items.
                mSubscription.request(nextRequestSize());

                // Reset the counter.
                mItemsProcessedSinceLastRequest = 0;
            }
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

