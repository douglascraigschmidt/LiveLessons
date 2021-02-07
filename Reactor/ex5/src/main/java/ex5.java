import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import lombok.SneakyThrows;
import tests.ReactorTests;
import tests.RxJavaTests;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This program applies reactive streams features to implement a
 * multi-tier Airline Booking App (ABA) as an Intellij project.  The
 * ABA project will be used as a motivating example throughout our
 * upcoming Coursera MOOC Specialization on Developing Secure and
 * Scalable Restful APIs for Reactive Microservices.  This six-part
 * MOOC Specialization is expected to launch at some point in 2021.
 * 
 * The ABA project showcases a wide range of Java concurrency and
 * parallelism frameworks that are used to synchronously and
 * asynchronously communicate with various microservices to find
 * prices for flights and convert these prices from US dollars to
 * other currencies.  These price are displayed after the
 * microservices complete their computations.
 *
 * The current version of ABA applies the Project Reactor and RxJava
 * reactive streams implementations together with the Spring WebFlux
 * reactive web application framework.  Other implementations will
 * showcase Java frameworks that provide concurrent object-oriented
 * programming and functional parallel programming capabilities (such
 * as the Java executor, parallel streams, and completable futures
 * frameworks), conventional RESTful microservices (such as Spring
 * Boot), and object-oriented and reactive database programming models
 * (such as the JPA and R2DBC).
 *
 * This ABA client provides a simple console app developed and run
 * using Intellij.  The MOOC specialization will also include an
 * Android client that provides a more interesting GUI app developed
 * and run using Android Studio.
 */
public class ex5 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The trip flight leg used for the tests.
     */
    private final TripRequest mTrip = TripRequest
        .valueOf(LocalDateTime.parse("2025-01-01T07:00:00"),
                 LocalDateTime.parse("2025-02-01T19:00:00"),
                 "LHR",
                 "JFK");

    /**
     * Indicate a conversion of US dollars to British pounds.
     */
    private final CurrencyConversion mCurrencyConversion = CurrencyConversion
        .valueOf("USD", "GBP", Options.instance().defaultRate());

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
            // This test uses Project Reactor to invoke microservices
            // that asynchronously determine the best price for a
            // flight from London to New York city in British pounds.
            .timeRun(() -> ReactorTests.runAsyncTests(mTrip,
                                                      mCurrencyConversion),
                     "runAsyncTests");

        RunTimer
            // This test uses RxJava to invoke microservices that
            // asynchronously determine the best price for a flight
            // from London to New York city in British pounds.
            .timeRun(()-> RxJavaTests.runAsyncTestsRx(mTrip,
                                                      mCurrencyConversion),
                     "runAsyncTestsRx");

        RunTimer
            // This test uses Project Reactor to invoke microservices
            // that synchronously determine the best price for a
            // flight from London to New York city in British pounds.
            .timeRun(() -> ReactorTests.runSyncTests(mTrip,
                                                     mCurrencyConversion),
                     "runSyncTests");

        // Print the results sorted from fastest to slowest.
        Options.print(RunTimer.getTimingResults());
    }
}
    
