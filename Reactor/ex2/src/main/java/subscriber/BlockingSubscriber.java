package subscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import utils.ExceptionUtils;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Define a Subscriber implementation that handles blocking, which is
 * otherwise not well-supported by Project Reactor.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class BlockingSubscriber<T>
       implements Subscriber<T>,
                  Disposable {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The calling thread uses this Barrier synchronizer to wait for a
     * subscriber to complete all its async processing.
     */
    final CountDownLatch mLatch;

    /**
     * The consumer to invoke on each onNext() value.
     */
    private final Consumer<? super T> mConsumer;

    /**
     * The consumer to invoke on onError() error signal.
     */
    private final Consumer<? super Throwable> mErrorConsumer;

    /**
     * The runnable to invoke on onComplete() complete signal.
     */
    private final Runnable mCompleteRunnable;

    /**
     * The strictly positive number of elements
     * to requests to the upstream Publisher.
     */
    private final long mN;

    /**
     * Keeps track of whether we've been disposed.
     */
    private boolean mIsDisposed;

    /**
     * Pass and store params that will respectively consume all the
     * elements in the sequence, handle errors and react to completion.
     *
     * @param consumer The consumer to invoke on each value
     * @param errorConsumer The consumer to invoke on error signal
     * @param completeRunnable The consumer to invoke on complete signal
     * @param n The strictly positive number of elements
     *          to requests to the upstream Publisher
     */
    public BlockingSubscriber(Consumer<? super T> consumer,
                              Consumer<? super Throwable> errorConsumer,
                              Runnable completeRunnable,
                              long n) {
        mLatch = new CountDownLatch(1);
        mConsumer = consumer;
        mErrorConsumer = errorConsumer;
        mCompleteRunnable = completeRunnable;
        mN = n;
    }

    /**
     * Block until all events have been processed by subscribe().
     */
    public void await() {
        // Block caller until the latch is released.
        ExceptionUtils.rethrowRunnable(mLatch::await);
    }

    /**
     * Hook method invoked after calling subscribe(subscriber) below.
     * No data starts flowing until s.request(long) is invoked.
     */
    @Override
    public void onSubscribe(Subscription s) {
        // Set the backpressure value.
        s.request(mN);
    }

    /**
     * Process the next element in the stream.
     * @param t The next element {@link T} in the stream
     */
    @Override
    public void onNext(T t) {
        // Run the consumer's hook method.
        mConsumer.accept(t);
    }

    /**
     * Handle an error event.
     * @param t The exception that occurred
     */
    @Override
    public void onError(Throwable t) {
        // Run the errorConsumer's hook method.
        mErrorConsumer.accept(t);

        // Release the latch.
        mLatch.countDown();
    }

    /**
     * Handle final completion event.
     */
    @Override
    public void onComplete() {
        // Run the completeConsumer's hook method.
        mCompleteRunnable.run();

        // Release the latch.
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
}
