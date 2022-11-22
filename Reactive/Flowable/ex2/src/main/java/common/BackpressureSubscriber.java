package common;

import io.reactivex.rxjava3.disposables.Disposable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import utils.PrimeUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An RxJava {@link Subscriber} that can implement backpressure.
 */
public class BackpressureSubscriber
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
     * {@link Subscription} used to control backpressure.
     */
    private Subscription mSubscription;

    /**
     * Keeps track of whether we've been disposed.
     */
    private boolean mIsDisposed;

    /**
     * The number of items to request from upstream.
     */
    private final int mRequestSize;

    /**
     * Number of items processed since the last request was sent.
     */
    private final AtomicInteger mItemsProcessedSinceLastRequest =
        new AtomicInteger(0);

    /**
     * Number of items processed overall.
     */
    private final AtomicInteger mItemsProcessed =
        new AtomicInteger(0);

    /**
     * Constructor initializes the fields.
     */
    public BackpressureSubscriber(AtomicInteger pendingItemCount,
                                  int requestSize) {
        // Initially false.
        mIsDisposed = false;

        // Store a reference to this count.
        mPendingItemCount = pendingItemCount;
        
        // The size to pass to mSubscription.
        mRequestSize = requestSize;
    }

    /**
     * Hook method called by the {@link Publisher} when this {@link
     * Subscriber} is first subscribed.  It sets the initial request
     * size.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        // Store the subscription for later use.
        mSubscription = subscription;

        // Set the initial request size.
        mSubscription.request(mRequestSize);
    }

    /**
     * Hook method called when next item arrives.  It prints the
     * results of prime # checking and may issue the next request.
     */
    @Override
    public void onNext(PrimeUtils.Result result) {
        if (Options.instance().printIteration(mItemsProcessed.incrementAndGet())) {
            // Print the results of prime number checking
            if (result.smallestFactor() != 0) {
                Options.debug("["
                        + mItemsProcessed
                        + "] "
                        + result.primeCandidate()
                        + " is not prime with smallest factor "
                        + result.smallestFactor());
            } else {
                Options.debug("["
                        + mItemsProcessed
                        + "] "
                        + result.primeCandidate()
                        + " is prime");
            }
        }

        // Print the current pending item count.
        Options.debug(TAG,"subscriber pending items: "
                      + mPendingItemCount.decrementAndGet());

        // Check to see if we've consumed all our items in this tranche.
        if (mItemsProcessedSinceLastRequest.incrementAndGet() == mRequestSize) {
            Options.debug("subscriber requesting next tranche of "
                          + mRequestSize
                          + " items");
            
            // Request the next tranche of items.
            mSubscription.request(mRequestSize);

            // Reset the counter.
            mItemsProcessedSinceLastRequest.set(0);
        }
    }

    /**
     * Hook method called to handle an error by printing the
     * exception.
     */
    @Override
    public void onError(Throwable t) { 
        Options.print("failure " + t);
    }

    /**
     * Hook method that's called when all integers have been
     * processed.
     */
    @Override
    public void onComplete() {
        Options.print("completed " + mItemsProcessed + " prime number checks");
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
}

