import utils.ExceptionUtils;

import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowRunnable;

/**
 * This program shows how the Java volatile type qualifier can be
 * used to enable two Thread objects to alternate printing "ping"
 * and "pong." More information about this technique appears at
 * <a href="https://dzone.com/articles/java-volatile-keyword-0">this link</a>.
 */
public class ex31 {
    /**
     * The volatile value that's shared between threads.
     */
    private static volatile long mVal = 0;

    /**
     * Max number of iterations.
     */
    private static final int sMAX_ITERATIONS = 5;

    /**
     * A static main() entry point method is needed to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.
        playPingPong();
    }

    /**
     * Create/start two threads that alternate printing
     * "ping" and "pong".
     */
    public static void playPingPong() {
        // Create and start a new "pong" Thread whose Runnable
        // lambda reacts to changes to mVal;
        new Thread(() -> {
            // Keep spinning until 'lv' == sMAX_ITERATIONS.
            for (long lv = mVal, // Atomically read 'lv' from mVal.
                 spins = 0;
                 lv < sMAX_ITERATIONS;
                 spins++)
                // Only run the body of this if statement
                // when 'lv' changes.
                if (lv != mVal) {
                    print("pong(" + mVal + ")"
                          + " [" + spins + " spins]");;

                    // Atomically read 'lv' from mVal.
                    lv = mVal;

                    // Reset the spin count.
                    spins = 0;
                }
        }, "pongThread").start();

        // Create and start a new "ping" Thread whose Runnable
        // lambda changes the value of mVal on each loop iteration.
        new Thread(() -> {
            // Keep looping until 'lv' == sMAX_ITERATIONS.
            for (long lv = mVal; // Atomically read 'lv' from mVal.
                 mVal < sMAX_ITERATIONS;
            ) {
                // Increment and print 'lv'.
                print("ping(" + ++lv + ")");

                // Set volatile mVal to the updated value of lv.
                mVal = lv;

                // Pause for 1 second to give the 'pong' Thread
                // a chance to run.
                rethrowRunnable(() -> Thread.sleep(1000));
            }
        }, "pingThread").start();
    }

    /**
     * Print parameter {@code s} prefixed with thread info.
     */
    private static void print(String s) {
        System.out.println(Thread.currentThread().getName()
            + ": "
            + s);
    }
}


