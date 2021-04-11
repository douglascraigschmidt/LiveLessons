package utils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;

/**
 * Define a Subscriber implementation that handles blocking.
 */
public class BlockingSubscriber
       implements Subscriber<BigFraction> {
    /**
     * The calling thread uses this Barrier synchronizer to wait for a
     * subscriber to complete all its async processing.
     */
    final CountDownLatch mLatch;

    /**
     * A StringBuilder used to log the output.
     */
    final StringBuilder mSb;

    /**
     * Constructor initializes the fields.
     */
    public BlockingSubscriber(StringBuilder stringBuilder) {
        mLatch = new CountDownLatch(1);
        mSb = stringBuilder;
    }

    /**
     * Block until all events have been processed by subscribe().
     */
    public void await() {
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook method invoked after calling subscribe(subscriber)
     * below.  No data starts flowing until s.request(long) is
     * invoked.
     */
    @Override
    public void onSubscribe(Subscription s) {
        // Disable backpressure.
        s.request(Integer.MAX_VALUE);
    }

    /**
     * Process the next element in the stream.
     * @param bigFraction The next BigFraction in the stream
     */
    @Override
    public void onNext(BigFraction bigFraction) {
        // Add fraction to the string buffer.
        mSb.append(" = " + bigFraction.toMixedString() + "\n");
    }

    /**
     * Handle an error event.
     * @param t The exception that occurred
     */
    @Override
    public void onError(Throwable t) {
        // Append the error message to the
        // StringBuilder.
        mSb.append(t.getMessage());

        // Display results when processing is done.
        BigFractionUtils.display(mSb.toString());

        // Release the latch.
        mLatch.countDown();
    }

    /**
     * Handle final completion event.
     */
    @Override
    public void onComplete() {
        // Display results when processing is done.
        BigFractionUtils.display(mSb.toString());

        // Release the latch.
        mLatch.countDown();
    }
}

