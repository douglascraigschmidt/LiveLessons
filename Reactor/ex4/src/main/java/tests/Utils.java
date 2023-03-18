package tests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * This Java utility class defines static methods and fields used by
 * other classes.
 */
public class Utils {
    /**
     * A Java utility class should have a private constructor.
     */
    private Utils() {}

    /**
     * The default exchange rate if a timeout occurs.
     */
    public static final Mono<Double> sDEFAULT_RATE_S =
            Mono.just(1.0);

    /**
     * The default exchange rate if a timeout occurs.
     */
    public static final Flux<Double> sDEFAULT_RATE_F =
            Flux.just(1.0);

    /**
     * The random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * This method simulates a webservice that finds the best price in
     * US dollars for a given flight leg.
     */
    public static double findBestPrice(String flightLeg, StringBuffer sb) {
        // Delay for a random amount of time.
        randomDelay();

        // Debugging print.
        sb.append("\nFlight leg is "
                + flightLeg);

        // Simply return a constant.
        return 888.00;
    }

    /**
     * This method simulates a webservice that finds the exchange rate
     * between a source and destination currency format.
     */
    public static double queryExchangeRateFor(String sourceAndDestination,
                                              StringBuffer sb) {
        String[] sAndD = sourceAndDestination.split(":");

        // Delay for a random amount of time.
        randomDelay();

        // Debugging print.
        sb.append("\nRate comparision between "
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
    public static double convert(double price, double rate) {
        return price * rate;
    }

    /**
     * Simulate a random delay between 0.5 and 4.5 seconds.
     */
    public static void randomDelay() {
        int delay = 500 + sRandom.nextInt(4000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
        }
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    public static void display(String string) {
        System.out.println("["
                + Thread.currentThread().getId()
                + "] "
                + string);
    }
}
