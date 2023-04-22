package quotes.utils;

import org.springframework.lang.NonNull;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import quotes.common.Options;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Define a backpressure-aware {@link CoreSubscriber} implementation
 * that handles blocking (which is otherwise not well-supported by
 * Project Reactor).
 */
public class BackpressureSubscriber<T>
       implements CoreSubscriber<T> {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

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
    private final long mRequestSize;

    /**
     * Holds the {@link Subscription} received from the publisher.
     */
    private Subscription mSubscription;

    /**
     * Keep track of the number of events processed thus far.
     */
    private final AtomicInteger mProcessedEvents =
        new AtomicInteger(0);

    /**
     * Track the total number of events received.
     */
    private final AtomicInteger mTotalEvents =
        new AtomicInteger(0);

    /**
     * The calling thread uses this Barrier synchronizer to wait for a
     * subscriber to complete all its async processing.
     */
    final CountDownLatch mLatch;

    /**
     * Pass and store params that will respectively consume all the
     * elements in the sequence, handle errors and react to completion.
     *
     * @param consumer         The consumer to invoke on each value
     * @param errorConsumer    The consumer to invoke on error signal
     * @param completeRunnable The consumer to invoke on complete signal
     * @param n                The strictly positive number of elements
     *                         to requests to the upstream Publisher
     */
    public BackpressureSubscriber(Consumer<? super T> consumer,
                                  Consumer<? super Throwable> errorConsumer,
                                  Runnable completeRunnable,
                                  long n) {
        mConsumer = consumer;
        mErrorConsumer = errorConsumer;
        mCompleteRunnable = completeRunnable;
        mRequestSize = n;
        mLatch = new CountDownLatch(1);
    }

    /**
     * Hook method invoked by the {@link Publisher} to start the stream
     * processing after the {@link CoreSubscriber} has subscribed.
     */
    @Override
    public void onSubscribe(@NonNull Subscription subscription) {
        mSubscription = subscription;

        // Set the backpressure value (no data starts flowing
        // until request() is invoked).
        mSubscription.request(mRequestSize);
    }

    /**
     * Process the next element in the stream.
     *
     * @param element The next element {@link T} in the stream
     */
    @Override
    public void onNext(T element) {
        // Run the consumer's hook method.
        mConsumer.accept(element);

        // Increment the count of total events received thus far.
        mTotalEvents.incrementAndGet();

        // Check if we've reached the request size.
        if (mProcessedEvents.incrementAndGet() == mRequestSize) {
            Options.debug(TAG, "Requesting size "
                + mRequestSize
                + " more events\n");

            // Request another chunk of requests.
            mSubscription.request(mRequestSize);

            // Reset the count of processed events.
            mProcessedEvents.set(0);
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
        Options.debug(TAG,
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
        Options.debug(TAG,
                totalEvents()
                   + " async computations completed successfully\n");

        // Run the completeRunnable's hook method.
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

    /**
     * Block until all events have been processed by subscribe().
     *
     * @return A {@link Mono} that completes when all events have been
     *         processed
     */
    public Mono<Void> await() {
        // Add some useful diagnostic output.
        Options.debug(TAG, "Waiting for async computations to complete.");

        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return an empty Mono.
        return Mono.empty();
    }
}
