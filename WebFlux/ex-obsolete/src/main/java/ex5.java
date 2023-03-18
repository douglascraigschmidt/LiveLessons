import clients.COOPClient;
import clients.RSocketClient;
import clients.ReactorClient;
import clients.RxJavaClient;
import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import microservices.apigateway.APIGatewayProxySync;
import utils.DateUtils;
import utils.Options;
import utils.RunTimer;

/**
 * This program applies reactive streams and reactive web programming
 * features to implement a multi-tier Airline Booking App (ABA) as an
 * Intellij project.  This version of ABA applies the Project Reactor
 * and RxJava reactive streams implementations together with the
 * Spring WebFlux reactive web application framework.  This ABA client
 * provides a simple console app developed and run using Intellij.
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
        .valueOf(DateUtils.parse("2025-01-01T07:00:00"),
                 DateUtils.parse("2025-02-01T19:00:00"),
                 "LHR",
                 "JFK",
                 1);

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
        /*
         APIGatewayProxySync a = new APIGatewayProxySync();
        
        a
            .findAirportInfo()
            .forEach(System.out::println); */

        RunTimer
                // This test uses Java concurrent object-oriented
                // programming frameworks to invoke microservices that
                // synchronously determine the best price for a flight
                // from London to New York city in British pounds.
                .timeRun(() -> COOPClient.runSyncTests(mTrip,
                        mCurrencyConversion),
                        "runCOOPSyncTests");

        RunTimer
            // This test uses RxJava to invoke microservices that
            // asynchronously determine the best price for a flight
            // from London to New York city in British pounds.
            .timeRun(()-> RxJavaClient.runAsyncTestsRx(mTrip,
                                                      mCurrencyConversion),
                     "runRxJavaAsyncTests");

        RunTimer
                // This test uses Project Reactor to invoke microservices
                // that asynchronously determine the best price for a
                // flight from London to New York city in British pounds.
                .timeRun(() -> ReactorClient.runAsyncTests(mTrip,
                        mCurrencyConversion),
                        "runReactorAsyncTests");

        RunTimer
                // This test uses the Spring RSocket and WebFlux
                // programming frameworks to invoke microservices that
                // synchronously determine the best price for a flight
                // from London to New York city in British pounds.
                .timeRun(() -> RSocketClient.runAsyncTests(mTrip,
                        mCurrencyConversion),
                        "runRSocketAsyncTests");

        // Print the results sorted from fastest to slowest.
        Options.print(RunTimer.getTimingResults());
    }
}
    
