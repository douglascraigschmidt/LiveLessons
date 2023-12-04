import common.BackpressureEmitter;
import common.BackpressureSubscriber;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import common.Options;
import org.reactivestreams.Subscriber;
import utils.PrimeUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This program applies RxJava {@link Flowable} features to
 * demonstrate how a {@link Subscriber} running in one {@link
 * Scheduler} context can exert flow-control on a backpressure-aware
 * {@link Publisher} that runs in a different {@link Scheduler}
 * context.
 */
public class ex2 {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = ex2.class.getSimpleName();

    /**
     * Count the # of pending items between {@link Publisher} and
     * {@link Subscriber}.
     */
    private static final AtomicInteger sPendingItemCount =
        new AtomicInteger(0);

    /**
     * Count the # of integers emitted by the {@link Publisher}.
     */
    private static int sIntegersEmitted;

    /**
     * Constructor initializes the fields.
     */
    static public void main(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        Options.print("Starting test with count = "
                      + Options.instance().count());

        // A subscriber that implements a backpressure model.
        BackpressureSubscriber sSubscriber = new BackpressureSubscriber
            (sPendingItemCount, Options.instance().requestSize());

        // Create a new Scheduler that runs its task in a new Thread.
        Scheduler newThreadScheduler = Schedulers.newThread();

        // Track all disposables to dispose of them all at once.
        CompositeDisposable sDisposables =
            new CompositeDisposable(sSubscriber);

        ex2
            // Create a publisher that runs in a new scheduler thread
            // and return a Flowable that emits random Integer objects
            // at a rate determined by the subscriber.
            .publishRandomIntegers(newThreadScheduler)

            // Concurrently check each random # to see if it's prime.
            .flatMap(ex2.checkForPrimality())

            // Dispose of the Subscriber and newThreadScheduler
            // when the stream processing is done.
            .doFinally(() -> {
                sDisposables.dispose();
                newThreadScheduler.shutdown();
            })

            // The blocking subscriber sets the program in motion.
            .blockingSubscribe(sSubscriber);

        Options.print("test complete");
    }

    /**
     * Publish a stream of random numbers.
     *
     * @param scheduler {@link Scheduler} to publish the random
     *                  numbers on
     * @return A {@link Flowable} that publishes random numbers
     */
    private static Flowable<Integer> publishRandomIntegers
        (Scheduler scheduler) {
        return Flowable
            // This factory method emits a stream of random integers.
            .generate(BackpressureEmitter
                      // Emit a stream of random integers.
                      .makeEmitter(Options.instance().count(),
                                   Options.instance().maxValue(),
                                   sPendingItemCount))

            // Handle errors/exceptions gracefully.
            .onErrorResumeNext(error -> {
                    Options.debug(error.getMessage());
                    return Flowable.empty();
                })

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * @return A {@link Function} that asynchronously checks a number for
     *         primality on a given {@link Scheduler}
     */
    private static Function<Integer, Publisher<? extends PrimeUtils.Result>>
        checkForPrimality() {
        return number -> Flowable
            // This factory method emits the number.
            .fromCallable(() -> number)

            // Check the number for primality in the given scheduler.
            .observeOn(Options.instance().scheduler())

            // Check if the number is prime.
            .map(__ ->
                 PrimeUtils.checkIfPrime(number))

            // Indicate the Scheduler context where the processing was done.
            .doOnNext(item -> {
                if (Options.instance().printIteration(sIntegersEmitted++))
                    Options.debug("["
                        + (sIntegersEmitted - 1)
                        + "] published "
                        + item
                        + " with "
                        + sPendingItemCount.get()
                        + " pending");

            });
    }
}
    
