import common.NonBackpressureEmitter;
import common.Options;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import utils.PrimeUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This program applies RxJava {@link Flowable} features to demonstrate
 * various types of backpressure strategies (e.g., MISSING, BUFFER,
 * ERROR, LATEST, and DROP) between a {@link Publisher} and a {@link
 * Subscriber} that run in the context of different {@link Scheduler}
 * objects.
 */
public class ex1 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Count the # of pending items between {@link Publisher} and
     * {@link Subscriber}.
     */
    private final AtomicInteger mPendingItemCount =
        new AtomicInteger(0);

    /**
     * Number of items processed overall.
     */
    private final AtomicInteger mItemsProcessed =
        new AtomicInteger(0);

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to run the test.
        new ex1(argv).run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex1(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);
    }

    /**
     * Run the test and print the results.
     */
    private void run() {
        Options.print("Starting test with count = "
                      + Options.instance().count());

        this
            // Create a publisher that runs in a new scheduler thread
            // and returns a Flowable that emits random Integers using
            // one of several backpressure strategies.
            .publishRandomIntegers(Schedulers.newThread())

            // Concurrently check each random # to see if it's prime.
            .flatMap(checkForPrimality())

            // The blocking subscriber sets the program in motion.
            .blockingSubscribe(this::processNext,
                               throwable -> Options.print("failure " + throwable),
                               () -> Options.print("completed " + mItemsProcessed.get() + " prime number checks"));

        Options.print("test complete");
    }

    /**
     * Process the next {@link PrimeUtils.Result}.
     *
     * @param result The result of the primality check
     */
    private void processNext(PrimeUtils.Result result) {
        if (Options.instance().printIteration(mItemsProcessed.incrementAndGet())) {
            // Print the results of prime number checking.
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
    }

    /**
     * @return A {@link Function} that checks a number for primality
     *         on a given {@link Scheduler}
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
                 PrimeUtils.checkIfPrime(number));
    }

    /**
     * Publish a stream of random numbers.
     *
     * @param scheduler {@link Scheduler} to publish the random
     *                  numbers on
     * @return A {@link Flowable} that publishes random numbers
     */
    private Flowable<Integer> publishRandomIntegers(Scheduler scheduler) {
        return Flowable
            // This factory method emits a stream of random integers.
            .create(NonBackpressureEmitter
                    // Emit a stream of random integers.
                    .makeEmitter(Options.instance().count(),
                                 Options.instance().maxValue(),
                                 mPendingItemCount),
                    // Set the overflow strategy.
                    Options.instance().overflowStrategy())

            // Handle errors/exceptions gracefully.
            .onErrorResumeNext(error -> {
                    Options.debug(error.getMessage());
                    return Flowable.empty();
                })

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }
}
    
