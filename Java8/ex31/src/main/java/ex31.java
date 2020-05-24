/**
 * This class demonstrates how the Java volatile type qualifier
 * can be used to enable two threads to alternate printing
 * "ping" and "pong," using a technique described at URL
 * https://dzone.com/articles/java-volatile-keyword-0.
 */
public class ex31 {
    /**
     * Sleep for {@code milliseconds}.
     */
    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out parameter {@code s} prefixed with thread info.
     */
    private static void print(String s) {
        System.out.println(Thread.currentThread()
                           + ": "
                           + s);
    }

    /**
     * Demonstrates how the Java volatile type qualifier can
     * be used to coordinate behavior between two threads that
     * alternatve printing "ping" and "pong".
     */
    static class PingPongTest {
        /**
         * The volatile value that's shared between threads.
         */
        private volatile int mVal = 0;

        /**
         * Max number of iterations.
         */
        private int sMAX_ITERATIONS = 5;

        /**
         * Create/start two threads that alternate printing
         * "ping" and "pong".
         */
        public void playPingPong() {
            // Create a new thread "pong" thread whose
            // runnable lambda listens for changes to mVal;
            new Thread(() -> {
                for (int lv = mVal; lv < sMAX_ITERATIONS; )
                    // Only do the body of the if statement when
                    // lv changes.
                    if (lv != mVal) {
                        print("pong(" + mVal + ")");
                        // Read lv from volatile mVal.
                        lv = mVal;
                    }
            }).start();

            // Create a new "ping" thread whose runnable lambda
            // changes the value of mVal on each loop iteration.
            new Thread(() -> {
                for (int lv = mVal; mVal < sMAX_ITERATIONS; ) {
                    print("ping(" + ++lv + ")");
                    
                    // Set volatile mVal to next value of lv.
                    mVal = lv;
                    sleep(500);
                }
            }).start();
        }
    }

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.
        new PingPongTest().playPingPong();
    }
}


