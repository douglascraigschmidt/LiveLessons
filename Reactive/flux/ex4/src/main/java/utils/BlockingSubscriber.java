package utils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static utils.BigFractionUtils.sVoidM;

/**
 * Define a Subscriber implementation that handles blocking, which is
 * otherwise not well-supported by Project Reactor.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class BlockingSubscriber<T>
    implements Subscriber<T> {
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
    private final long mWindowSize;

    /**
     * The {@link StringBuffer} to write debug messages into.
     */
    private final StringBuffer mSb;

    /**
     * Keep track of the number of events processed thus far.
     */
    private long mEventsProcessedThusFar;

    /**
     * Holds the {@link Subscription} received from the publisher.
     */
    private Subscription mSubscription;

    /**
     * Track the total number of events received.
     */
    private final AtomicInteger mTotalEvents = new AtomicInteger(0);

    /**
     * Pass and store params that will respectively consume all the
     * elements in the sequence, handle errors and react to completion.
     *
     * @param consumer         The consumer to invoke on each value
     * @param errorConsumer    The consumer to invoke on error signal
     * @param completeRunnable The consumer to invoke on complete signal
     * @param n                The strictly positive number of elements
     *                         to requests to the upstream Publisher
     * @param sb               The {@link StringBuffer} to write debug statements into
     */
    public BlockingSubscriber(Consumer<? super T> consumer,
                              Consumer<? super Throwable> errorConsumer,
                              Runnable completeRunnable,
                              long n,
                              StringBuffer sb) {
        mLatch = new CountDownLatch(1);
        mConsumer = consumer;
        mErrorConsumer = errorConsumer;
        mCompleteRunnable = completeRunnable;
        mWindowSize = n;
        mEventsProcessedThusFar = 0;
        mSb = sb;

        // Add some useful diagnostic output.
        sb.append("["
            + Thread.currentThread().getId()
            + "] "
            + "Starting async processing.\n");
    }

    /**
     * Block until all events have been processed by subscribe().
     *
     * @return An empty {@link Mono} to indicate to the caller that
     * all processing is done
     */
    public Mono<Void> await() {
        // Add some useful diagnostic output.
        mSb.append("["
                   + Thread.currentThread().getId()
                   + "] "
                   + "Waiting for async computations to complete.\n");

        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return empty Mono to indicate to the caller that all
        // processing is done.
        return sVoidM;
    }

    /**
     * Hook method invoked after calling subscribe(subscriber) below.
     * No data starts flowing until s.request(long) is invoked.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        mSubscription = subscription;

        // Set the backpressure value.
        mSubscription.request(mWindowSize);
    }

    /**
     * Process the next element in the stream.
     *
     * @param t The next element {@link T} in the stream
     */
    @Override
    public void onNext(T t) {
        // Run the consumer's hook method.
        mConsumer.accept(t);

        mTotalEvents.incrementAndGet();
        if (++mEventsProcessedThusFar == mWindowSize) {
            mSb.append("Requesting size "
                + mWindowSize
                + " more events\n");
            mSubscription.request(mWindowSize);
            mEventsProcessedThusFar = 0;
        }
    }

    /**
     * Handle an error event.
     *
     * @param t The exception that occurred
     */
    @Override
    public void onError(Throwable t) {
        // Add the total number of events processed.
        mSb.append("["
                   + Thread.currentThread().getId()
                   + "] "
                   + totalEvents()
                   + " async computations completed successfully,\n    but then received "
                   + t.getClass().getSimpleName()
                   + ":\n    "
                   + t.getMessage());

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
        // Add the total number of events processed.
        mSb.append("["
                   + Thread.currentThread().getId()
                   + "] "
                   + totalEvents()
                   + " async computations completed successfully\n");

        // Run the completeConsumer's hook method.
        mCompleteRunnable.run();

        // Release the latch.
        mLatch.countDown();
    }

    /**
     * @return The total number of events processed
     */
    public int totalEvents() {
        return mTotalEvents.get();
    }
}

