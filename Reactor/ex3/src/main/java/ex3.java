import common.Options;
import utils.PrimeUtils;
import publisher.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import subscriber.BackpressureSubscriber;
import utils.RandomUtils;
import utils.ReactorUtils;
import utils.RunTimer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Map.Entry.comparingByValue;
import static publisher.Publisher.sPendingItemCount;

/**
 * This program applies Project Reactor features to implement various
 * types of backpressure strategies (e.g., ignore, buffer, error,
 * latest, drop, and push/pull) between a publisher and a subscriber
 * that (conditionally) run in different threads/schedulers.
 */
public class ex3 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link Scheduler} used to publish random {@link Integer}
     * objects in its own thread.
     */
    private final Scheduler mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

    /**
     * The {@link Scheduler} used to consume random integers by checking
     * if they are prime or not.  This {@link Scheduler} can be either
     * sequential or parallel, depending on program options.
     */
    private final Scheduler mSubscriberScheduler;

    /**
     * A subscriber that applies backpressure.
     */
    private final BackpressureSubscriber mSubscriber =
        new BackpressureSubscriber(sPendingItemCount);

    /**
     * Track all disposables to dispose them all at once.
     */
    private final Disposable.Composite mDisposables;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to run the test.
        new ex3(argv).run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex3(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Conditionally run the subscriber in a different thread.
        mSubscriberScheduler = Options.instance().parallel()
            // Choose a different scheduler if we're running in
            // parallel.
            ? Schedulers.newParallel("subscriber",
                                     Options.instance().parallelism())

            // Otherwise run everything on the publisher's scheduler.
            : mPublisherScheduler;

        // Track all disposables to dispose them all at once.
        mDisposables = Disposables
            .composite(mPublisherScheduler,
                       mSubscriberScheduler,
                       mSubscriber);
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Generate a list of 'count' odd random Integers whose values
        // don't exceed the given maximum.
        var randomIntegers = RandomUtils
            .generateRandomIntegers(Options.instance().count(),
                                    Options.instance().maxValue(),
                                    true);

        // This Function asynchronously determines if a random # is
        // prime or not.
        Function<Integer, Mono<PrimeUtils.Result>> check4Prime =
            getPrimeCheckFunction(PrimeUtils::isPrime);

        Publisher
            // Create a publisher that runs on its own scheduler and
            // returns a Flux that emits random Integer objects.
            .publishIntegers(mPublisherScheduler,
                             randomIntegers)

            // Conditionally enable logging.
            .transform(ReactorUtils
                       // Conditionally enable logging.
                       .logIf(Options.instance().loggingEnabled()))

            // Concurrently (maybe) check each random # to see if it's
            // prime.  This operation may run on the subscriber's
            // scheduler, depending on the options used to run the
            // program.
            .flatMap(check4Prime)

            // The subscriber starts all the wheels in motion!
            .subscribe(mSubscriber);

        Options.debug(TAG, "waiting in the main thread");

        // BLock until all processing is complete.
        mSubscriber.await();

        // Dispose of all schedulers and subscribers.
        mDisposables.dispose();
    }

    /**
     * Get a {@link Function} that asynchronously determines if a
     * random # is prime or not.
     *
     * @param primeChecker A {@link Function} that checks a number's
     *                     primality
     * @return A {@link Function} that asynchronously determines if a
     *         random # is prime or not
     */
    private Function<Integer,
                     Mono<PrimeUtils.Result>> getPrimeCheckFunction
        (Function<Integer, Integer> primeChecker) {
        return number -> Mono
            .fromCallable(() -> number)

            // Subscriber may run in different thread(s).
            .publishOn(mSubscriberScheduler)

            // Check if the # is prime.
            .map(__ -> PrimeUtils
                 .checkIfPrime(number,
                               primeChecker));
    }
}
    
