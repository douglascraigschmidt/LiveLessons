import common.Options;
import utils.PrimeUtils;
import org.reactivestreams.Subscriber;
import publisher.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import subscriber.BlockingSubscriber;
import utils.RandomUtils;
import utils.ReactorUtils;

import java.util.function.Function;

import static publisher.Publisher.sPendingItemCount;

/**
 * This program applies Project Reactor features to check the primality
 * of randomly generate {@link Integer} objects via a publisher and
 * a subscriber that (conditionally) run in different threads/schedulers.
 */
public class ex2 {
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

    // Track all disposables to dispose them all at once.
    /**
     * A {@link Subscriber} that blocks the caller.
     */
    private final BlockingSubscriber<PrimeUtils.Result> mSubscriber =
        new BlockingSubscriber<>(result -> {


                // Store the current pending item count.
                int pendingItems = Publisher
                    .sPendingItemCount.decrementAndGet();

                if (Options.instance().printDiagnostic(pendingItems)) {
                    // Print the results of prime number checking.
                    PrimeUtils.printResult(result);
                    Options.debug(TAG, "subscriber pending items: "
                        + pendingItems);
                }
        },
        throwable -> {
            Options.print("failure " + throwable);
        },
        () -> Options.print("completed"),
        Integer.MAX_VALUE);

    /**
     * Track all disposables to dispose them all at once.
     */
    private final Disposable.Composite mDisposables;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to run the test.
        new ex2(argv).run(PrimeUtils::isPrime,
            "pub/sub prime checker");
    }

    /**
     * Constructor initializes the fields.
     */
    ex2(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Conditionally run the subscriber in a different thread.
        mSubscriberScheduler = Options.instance().parallel()
            // Choose a different scheduler if we're running in parallel.
            ? Schedulers.newParallel("subscriber",
            Options.instance().parallelism())

            // Otherwise run everything on the publisher's scheduler.
            : mPublisherScheduler;

        mDisposables = Disposables
            .composite(mPublisherScheduler,
                mSubscriberScheduler,
                mSubscriber);
    }

    /**
     * Run the prime number test.
     *
     * @param primeChecker A {@link Function} that maps candidate
     *                     primes to their smallest factor (if they
     *                     aren't prime) or 0 if they are prime
     * @param testName     Name of the test
     */
    @SuppressWarnings("SameParameterValue")
    private void run
    (Function<Integer, Integer> primeChecker,
     String testName) {
        Options.print("Starting "
            + testName
            + " with count = "
            + Options.instance().count());

        // Generate a list of 'count' odd random Integers whose values
        // don't exceed the given maximum.
        var randomIntegers = RandomUtils
            .generateRandomIntegers(Options.instance().count(),
                Options.instance().maxValue(),
                true);

        // This Function asynchronously determines if a random # is
        // prime or not.
        Function<Integer, Mono<PrimeUtils.Result>> check4Prime =
            makePrimeCheckFunction(primeChecker);

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

        // Print the results.
        Options.print(makeExitString(testName, primeChecker));

        // Release the Schedulers and Subscriber.
        mDisposables.dispose();
    }

    /**
     * Get a {@link Function} that  asynchronously determines if a
     * random # is prime or not.
     *
     * @param primeChecker A {@link Function} that checks a number's
     *                     primality
     * @return A {@link Function} that  asynchronously determines if a
     * random # is prime or not
     */
    private Function<Integer,
        Mono<PrimeUtils.Result>> makePrimeCheckFunction
    (Function<Integer, Integer> primeChecker) {
        return number -> Mono
            .fromSupplier(() -> number)

            // Subscriber may run in different thread(s).
            .publishOn(mSubscriberScheduler)

            // Check if the # is prime.
            .map(__ -> PrimeUtils
                .checkIfPrime(number,
                    primeChecker));
    }

    /**
     * Create and return the formatted exit string based on various
     * conditions.
     *
     * @return The formatted exit string
     */
    private String makeExitString(String testName,
                                  Function<Integer, Integer> primeChecker) {
        return "Leaving "
            + testName
            + " with "
            + PrimeUtils.sPrimeCheckCounter.get()
            + " prime checks ";
    }
}
    
