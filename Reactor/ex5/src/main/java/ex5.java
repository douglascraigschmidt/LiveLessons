import datamodels.CurrencyConversion;
import datamodels.Trip;
import io.reactivex.rxjava3.core.Single;
import tests.ReactorTests;
import tests.RxJavaTests;
import utils.Options;
import utils.RunTimer;

import java.time.LocalDate;

/**
 * This program applies WebFlux and Project Reactor features to
 * implement an airline reservations app that synchronously and
 * asynchronously communicates with various microservices that find
 * the best price for flight legs and convert from US dollars into
 * other currencies.  The best price is displayed after the
 * microservices have completed their computations.
 */
public class ex5 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The trip used for the tests.
     */
    private Trip mTrip = Trip
        .valueOf(LocalDate.parse("2025-01-01"),
                 LocalDate.parse("2025-02-02"),
                 "LHR",
                 "JFK");

    /**
     * Indicate a conversion of US dollars to British pounds.
     */
    private CurrencyConversion mCurrencyConversion = CurrencyConversion
        .valueOf("USD", "GBP");

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(args);

        // Run the test program.
        new ex5().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        RunTimer
            // This test invokes microservices to asynchronously
            // determine the best price for a flight from London to
            // New York city in British pounds.
            .timeRun(() -> ReactorTests.runAsyncMonos(mTrip,
                                                      mCurrencyConversion),
                     "runAsyncMonos");

        RunTimer
            // This test invokes microservices to synchronously determine
            // the best price for a flight from London to New York city in
            // British pounds.
            .timeRun(() -> ReactorTests.runSyncMonos(mTrip,
                                                     mCurrencyConversion),
                     "runSyncMonos");

        RunTimer
            // This test invokes microservices to asynchronously determine
            // the best price for a flight from London to New York city in
            // British pounds.
            .timeRun(()-> RxJavaTests.runAsyncSingles(mTrip,
                                                      mCurrencyConversion),
                     "runAsyncSingles");

        // Print the results sorted from fastest to slowest.
        Options.print(RunTimer.getTimingResults());
    }
}
    
