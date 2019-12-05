import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
     *
     */
    private static final double sDEFAULT_RATE = 1.0;

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
        CompletableFuture
            .supplyAsync(() -> findBestPrice("LDN - NYC"))
            .thenCombine(CompletableFuture
                         .supplyAsync(() -> queryExchangeRateFor("USD", "GBP"))
                         .completeOnTimeout(sDEFAULT_RATE, 1, TimeUnit.SECONDS),
                         this::convert)
            .orTimeout(3, TimeUnit.SECONDS)
            .whenComplete((amount, ex) -> { 
                    if (amount != null) { 
                        System.out.println("The price is: " + amount + " GBP");
                    } else { System.out.println(ex.getMessage()); 
                    }   
                });
    }

    /**
     *
     */
    private double findBestPrice(String flightLeg) {
        return 888.00;
    }

    /**
     *
     */
    private double queryExchangeRateFor(String source, String destination) {
        return 1.20;
    }

    /**
     *
     */
    private double convert(double price, double rate) {
        return price * rate;
    }

}
