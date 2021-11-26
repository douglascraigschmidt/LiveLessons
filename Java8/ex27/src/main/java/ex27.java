import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This example shows how to apply Java 9 timeouts with the Java
 * completable futures framework.
 */
public class ex27 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex27.class.getName();

    /**
     * The default exchange rate if a timeout occurs.
     */
    private static final double sDEFAULT_RATE = 1.0;

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
        new ex27().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        // Iterate multiple times.
        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            print("Iteration #" + i);

            // Asynchronously find the best price in US dollars
            // between London and New York.
            CompletableFuture<Double> bestPriceF = CompletableFuture
                .supplyAsync(() -> findBestPrice("LDN - NYC"));

            // Asynchronously compute the exchange rate.
            CompletableFuture<Double> exchangeRateF = CompletableFuture
                // Asynchronously determine exchange rate between US
                // dollars and British pounds.
                .supplyAsync(() ->
                             queryExchangeRateFor("USD", "GBP"))

                // If this computation runs for more than 2 seconds
                // return the default rate.
                .completeOnTimeout(sDEFAULT_RATE,
                                   2,
                                   TimeUnit.SECONDS);

            // Call this::convert method reference when both previous
            // stages complete.
            bestPriceF
                .thenCombine(exchangeRateF,
                             // Convert the price in dollars to the
                             // price in pounds.
                             this::convert)

                // If async processing takes more than 3 seconds a
                // TimeoutException will be thrown.
                .orTimeout(3, TimeUnit.SECONDS)

                // This method always gets called, regardless of
                // whether an exception occurred or not.
                .whenComplete((amount, ex) -> {
                        if (amount != null) {
                            print("The price is: " 
                                  + amount 
                                  + " GBP");
                        } else {
                            print("The exception thrown was " 
                                  + ex.toString());
                        }
                    })

                // Swallow the exception.
                .exceptionally(ex -> null)

                // Block until all async processing completes.
                .join();
        }
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
    private double queryExchangeRateFor(String source, String destination) {
        // Delay for a random amount of time.
        randomDelay();

        // Debugging print.
        print("Rate comparision between " 
              + source
              + " and "
              + destination);

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
