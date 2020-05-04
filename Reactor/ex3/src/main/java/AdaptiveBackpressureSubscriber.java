import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import utils.Options;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A subscriber that implements adaptive backpressure.
 */
public class AdaptiveBackpressureSubscriber
       implements Subscriber<Result>,
                  Disposable {
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
     * Constructor initializes the field.
     */
    AdaptiveBackpressureSubscriber(AtomicInteger pendingItemCount) {
        // Initially false.
        mIsDisposed = false;

        // Store a reference to this count.
        mPendingItemCount = pendingItemCount;
    }

    /**
     * Hook method called when this subscriber is first
     * subscribed.  It sets the initial request size.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        // Store the subscription for later use.
        mSubscription = subscription;

        // Set the initial request size.
        mSubscription
            .request(nextRequestSize());                
    }

    /**
     * @return The next request size for the publisher, which
     * is computed adaptively.
     */
    int nextRequestSize() {
        int pendingItems = mPendingItemCount.get();

        if (pendingItems == 0) {
            Options.display("pending items == 0, returning 5");
            return 5;
        } else if (pendingItems > 3) {
            Options.display("pending items = " + pendingItems + ", returning 3");
            return 3;
        } else {
            Options.display("return pending items = " + pendingItems);
            return pendingItems;
        }
    }

    /**
     * Hook method called when the next item is received.  It prints
     * the results of the prime number checking and updates the next
     * request size.
     */
    @Override
    public void onNext(Result result) {
        // Print the results of prime number checking.
        if (result.mSmallestFactor != 0) {
            Options.display(result.mPrimeCandidate
                            + " is not prime with smallest factor "
                            + result.mSmallestFactor);
        } else {
            Options.display(result.mPrimeCandidate
                            + " is prime");
        }

        Options.display("consumer pending items: "
                        + mPendingItemCount.decrementAndGet());

            
        // Adaptively update the next request size.
        mSubscription
            .request(nextRequestSize());
    }

    /**
     * Hook method called to handle an error by printing the
     * exception.
     */
    @Override
    public void onError(Throwable t) { Options.display("failure" + t); }

    /**
     * Hook method that's called when all integers have been
     * processed.
     */
    @Override
    public void onComplete() { Options.display("completed"); }

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

