package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static utils.BigFractionUtils.sVoidC;

/**
 * Define a generic {@link Subscriber} implementation that handles blocking and
 * backpressure.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class BlockingSubscriber<T>
       implements Subscriber<T> {
    /**
     * The calling thread uses this barrier synchronizer to wait for a
     * subscriber to complete all its async processing.
     */
    final CountDownLatch mLatch;

    /**
     * The consumer to invoke on each onNext() value.
     */
    private final Consumer<? super T> mConsumer;

    /**
     * The consumer to invoke on error signal.
     */
    private final Consumer<? super Throwable> mErrorConsumer;

    /**
     * The consumer to invoke on complete signal.
     */
    private final Action mCompleteAction;

    /**
     * The strictly positive number of elements
     * to requests to the upstream Publisher.
     */
    private final long mN;

    /**
     * Pass and store params that will respectively consume all the
     * elements in the sequence, handle errors and react to completion.
     *
     * @param consumer The {@link Consumer} to invoke on each value
     * @param errorConsumer The {@link Consumer} to invoke on error signal
     * @param completeAction The {@link Action} to invoke on complete signal
     * @param n The strictly positive number of elements
     *          to requests to the upstream Publisher
     */
    public BlockingSubscriber(Consumer<? super T> consumer,
                              Consumer<? super Throwable> errorConsumer,
                              Action completeAction,
                              long n) {
        mLatch = new CountDownLatch(1);
        mConsumer = consumer;
        mErrorConsumer = errorConsumer;
        mCompleteAction = completeAction;
        mN = n;
    }

    /**
     * Block until all events have been processed by subscribe().
     *
     * @return A {@link Completable} to indicate to the caller that
     * all processing is done
     */
    public Completable await() {
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return a Completable to indicate to the caller that all
        // processing is done.
        return sVoidC;
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
     *
     * @param t The next element in the stream
     */
    @Override
    public void onNext(T t) {
        // Run the consumer's hook method.
        mConsumer.accept(t);
    }

    /**
     * Handle an error event.
     *
     * @param t The {@link Throwable} exception that occurred
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
        // Run the completeAction's hook method.
        try {
            mCompleteAction.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // Release the latch.
        mLatch.countDown();
    }
}

