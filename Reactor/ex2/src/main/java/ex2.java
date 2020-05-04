import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex2 {
    /**
     * Count the number of pending items.
     */
    private final AtomicInteger mPendingItemCount =
        new AtomicInteger(0);

    /**
     * Max number of random integers to generate.
     */
    private final int mCount = 100;

    /**
     * A list of randomly-generated large integers.
     */
    private final List<Integer> mRandomIntegers = IntStream
        .rangeClosed(1, mCount)

        // Convert each primitive int to Integer.
        .boxed()    
                   
        // Trigger intermediate operations and collect into list.
        .collect(toList());

    /**
     *
     */
    private final Scheduler mSubscriberScheduler;

    /**
     *
     */
    private final Scheduler mPublisherScheduler;

    /**
     *
     */
    private final AdaptiveBackpressureSubscriber mSubscriber;

    /**
     *
     */
    private final Disposable.Composite mDisposables;

    /**
     *
     */
    private final CountDownLatch mLatch;

    private class AdaptiveBackpressureSubscriber
            implements Subscriber<Integer>,
                       Disposable {
        private final Scheduler mScheduler;
        private Subscription mSubscription;
        private boolean mIsDisposed;

        AdaptiveBackpressureSubscriber(Scheduler scheduler) {
            mScheduler = scheduler;
            mIsDisposed = false;
        }
        /**
         *
         */
        @Override
        public void onSubscribe(Subscription subscription) {
            mSubscription = subscription;
            mSubscription
                .request(nextRequestSize());                
        }

        /**
         *
         */
        @Override
        public void onNext(Integer integer) {
            consume(integer, mScheduler);

            mSubscription
                .request(nextRequestSize());
        }

        /**
         *
         * @param t
         */
        @Override
        public void onError(Throwable t) { display("failure" + t); }

        /**
         *
         */
        @Override
        public void onComplete() { ;}

        /**
         *
         */
        @Override
        public void dispose() {
            mIsDisposed = true;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isDisposed() {
            return mIsDisposed;
        }
    }

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        ex2 test = new ex2();
        
        test.run();
    }

    /**
     *
     */
    ex2() {
        // Run the subscriber in a single thread.
        mSubscriberScheduler = Schedulers
            .newParallel("subscriber", 1);

        // Run the publisher in a different single thread.
        mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

        // Create a countdown latch that causes the main thread to block until
        // all the processing is done.
        mLatch = new CountDownLatch(1);

        // Create a subscriber that handles backpressure.
        mSubscriber =
            new AdaptiveBackpressureSubscriber(mPublisherScheduler);

        // Dispose of everything in one fell swoop.
        mDisposables = Disposables
            .composite(mPublisherScheduler,
                       mSubscriberScheduler,
                       mSubscriber);
    }

    /**
     *
     * @throws InterruptedException
     */
    private void run() throws InterruptedException {
        // Create a publisher that runs on its own scheduler.
        Flux<Integer> publisher = publisher(mPublisherScheduler);

        publisher
            // Arrange to release the latch when the subscriber is done.
            .doOnTerminate(mLatch::countDown)

            // Run the subscriber in a different thread.
            .publishOn(mSubscriberScheduler, nextRequestSize())

            // Start the wheels in motion.
            .subscribe(mSubscriber);

        display("waiting in the main thread");

        // Wait for all processing to complete.
        mLatch.await();

        // Dispose of all schedulers and subscribers.
        mDisposables.dispose();
    }

    /**
     *
     * @param scheduler
     * @return
     */
    private Flux<Integer> publisher(Scheduler scheduler) {
        // Iterate through all the random numbers.
        final Iterator<Integer> iterator =
            mRandomIntegers.iterator();

        return Flux
            // Generate a flux of random integers.
            .<Integer>create(sink -> sink.onRequest(size -> {
                        display("Request size = " + size);

                        // Try to publish size items.
                        for (int i = 0;
                             i < size;
                             ++i) {
                            // Keep going if there is an item remaining
                            // in the iterator.
                            if (iterator.hasNext()) {
                                // Get the next item.
                                Integer item = iterator.next();

                                display("published item: "
                                        + item
                                        + ", pending items = "
                                        + mPendingItemCount.incrementAndGet());

                                // Publish the next item.
                                sink.next(item);
                            } else {
                                // We're done publishing all the items.
                                sink.complete();
                                break;
                            }
                        }
                    }))

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     *
     * @param item
     * @param scheduler
     * @return
     */
    private Integer consume(Integer item, Scheduler scheduler) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            ;
        }
        display("processed item: "
                + item
                + ", pending items: "
                + mPendingItemCount.decrementAndGet());
        return item;

    }

    /**
     * @return The next request size for the publisher, which
     * is computed adaptively.
     */
    private int nextRequestSize() {
        int pendingItems = mPendingItemCount.get();

        if (pendingItems == 0) {
            display("pending items == 0, returning 5");
            return 5;
        } else if (pendingItems > 3) {
            display("pending items = " + pendingItems + ", returning 3");
            return 3;
        } else {
            display("return pending items = " + pendingItems);
            return pendingItems;
        }
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getName()
                           + "] "
                           + string);
    }
}
