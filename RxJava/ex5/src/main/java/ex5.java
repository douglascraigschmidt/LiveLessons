import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import utils.RxUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This example shows how to apply timeouts with the RxJava framework.
 */
public class ex5 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex5.class.getName();

    /**
     * The default exchange rate if a timeout occurs.
     */
    private static final Single<Double> sDEFAULT_RATE_S =
            Single.just(1.0);

    /**
     * The default exchange rate if a timeout occurs.
     */
    private static final Flowable<Double> sDEFAULT_RATE_F =
            Flowable.just(1.0);

    /**
     * The number of iterations to run the test.
     */
    private static final int sMAX_ITERATIONS = 5;

    /**
     * The random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.
        new ex5().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        // Run a test that demonstrates timeouts for RxJava concurrent
        // Singles.
        runConcurrentSingles();

        // Run a test that demonstrates timeouts for RxJava
        // ParallelFlowables.
        runParallelFlowables();
    }

    /**
     * Run a test that demonstrates timeouts for RxJava concurrent
     * Singles.
     */
    private void runConcurrentSingles() {
        System.out.println("begin runConcurrentSingles()");

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            print("Iteration #" + i);

            Single<Double> priceS = Single
                // Asynchronously find the best price in US dollars
                // between London and New York.
                .fromCallable(() -> findBestPrice("LDN - NYC"))

                // Run the computation in the common fork-join pool.
                .compose(RxUtils.commonPoolSingle());

            Single<Double> rateS = Single
                // Asynchronously determine exchange rate between US
                // dollars and British pounds.
                .fromCallable(() ->
                              queryExchangeRateFor("USD:GBP"))

                // If this computation runs for more than 2 seconds
                // return the default rate.
                .timeout(2, TimeUnit.SECONDS, sDEFAULT_RATE_S)

                // Run the computation in the common fork-join pool.
                .compose(RxUtils.commonPoolSingle());

            Single
                // Call this::convert method reference to convert the
                // price in dollars to the price in pounds when both
                // previous singles complete.
                .zip(priceS, rateS, this::convert)

                // If async processing takes more than 3 seconds a
                // TimeoutException will be thrown.
                .timeout(3, TimeUnit.SECONDS)

                // Block until all async processing completes.
                .blockingSubscribe(amount ->
                                   System.out.println("The price is: " 
                                                      + amount 
                                                      + " GBP"),
                                   ex ->
                                   System.out.println("The exception thrown was " 
                                                      + ex.toString()));
        }

        System.out.println("end runConcurrentSingles()");
    }

    /**
     * Run a test that demonstrates timeouts for RxJava
     * ParallelFlowables.
     */
    private void runParallelFlowables() {
        System.out.println("begin runParallelFlowables()");

        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            print("Iteration #" + i);

            Flowable<Double> priceF = Flowable
                // Asynchronously find the best price in US dollars
                // from London to New York.
                .just("LDN:NYC")

                // Run the computation in the common fork-join pool.
                .parallel().compose(RxUtils.commonPoolParallelFlowable())

                // Find the best price.
                .map(this::findBestPrice)

                // Convert back to sequential.
                .sequential();

            Flowable<Double> rateF = Flowable
                // Asynchronously determine exchange rate from British
                // pounds to US dollars.
                .just("GBP:USA")

                // Run the computation in the common fork-join pool.
                .parallel().compose(RxUtils.commonPoolParallelFlowable())

                // Find the exchange rate.
                .map(this::queryExchangeRateFor)

                // Convert back to sequential.
                .sequential()

                // If this computation runs for more than 2 seconds
                // return the default rate.
                .timeout(2, TimeUnit.SECONDS, sDEFAULT_RATE_F)                    ;

            Flowable
                // Call this::convert method reference to convert the
                // price in dollars to the price in pounds when both
                // previous singles complete.
                .zip(priceF, rateF, this::convert)

                // If async processing takes more than 3 seconds a
                // TimeoutException will be thrown.
                .timeout(3, TimeUnit.SECONDS)

                // Block until all async processing completes.
                .blockingSubscribe(amount ->
                                   System.out.println("The price is: " 
                                                      + amount 
                                                      + " GBP"),
                                   ex ->
                                   System.out.println("The exception thrown was " 
                                                      + ex.toString()));
        }

        System.out.println("end runParallelFlowables()");
    }

    /**
     * This method simulates a webservice that finds the best price in
     * US dollars for a given flight leg.
     */
    private double findBestPrice(String flightLeg) {
        // Delay for a random amount of time.
        randomDelay();
        
        // Debugging print.
        print("Flight leg is "
              + flightLeg);

        // Simply return a constant.
        return 888.00;
    }

    /**
     * This method simulates a webservice that finds the exchange rate
     * between a source and destination currency format.
     */
    private double queryExchangeRateFor(String sourceAndDestination) {
        String[] sAndD = sourceAndDestination.split(":");

        // Delay for a random amount of time.
        randomDelay();

        // Debugging print.
        print("Rate comparision between " 
              + sAndD[0]
              + " and "
              + sAndD[1]);

        // Simply return a constant.
        return 1.20;
    }

    /**
     * Convert a price in one currency system by multiplying it by the
     * exchange rate.
     */
    private double convert(double price, double rate) {
        return price * rate;
    }

    /**
     * Simulate a random delay between 0.5 and 4.5 seconds.
     */
    private static void randomDelay() {
        int delay = 500 + sRandom.nextInt(4000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Print the {@code string} together with thread information.
     */
    private void print(String string) {
        System.out.println("Thread["
                           + Thread.currentThread().getId()
                           + "]: "
                           + string);
    }
}
