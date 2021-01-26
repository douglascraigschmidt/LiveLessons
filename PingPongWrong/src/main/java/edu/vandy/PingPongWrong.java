import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;

/**
 * @class PingPongWrong
 *
 * @brief This class implements a Java program that creates two
 *        threads that attempt to alternately print "Ping" and "Pong",
 *        respectively, on the display.  This implementation behaves
 *        incorrectly due to lack of synchronization between the
 *        threads.  It's also hard-coded to run as a Java console
 *        application and thus can't work in Android without major
 *        changes.
 */
public class PingPongWrong
{
    /**
     * Number of iterations to play ping/pong.
     */
    public static int mMaxIterations = 10;
    
    /**
     * @brief PlayPingPongThread
     *
     * @class This class implements the incorrect non-synchronized
     *        version of the ping/pong application.
     */
    public static class PlayPingPongThread extends Thread
    {
        public PlayPingPongThread (String stringToPrint)
        {
            this.mStringToPrint = stringToPrint;
        }

        /**
         * Main event loop that runs in a separate thread of control.
         */
        public void run () 
        {
            for (int loopsDone = 1;
                 loopsDone <= mMaxIterations;
                 ++loopsDone) 
                // Print out the iteration.
                System.out.println(mStringToPrint + "(" + loopsDone + ")");

            // Exit the thread when the loop is done.
        }

        // The string to print for each ping and pong operation.
        private String mStringToPrint;
    }

    public static void main(String[] args) {
        try {         
            System.out.println("Ready...Set...Go!");

            // Create the ping and pong threads.
            PlayPingPongThread ping = 
                new PlayPingPongThread("Ping!");
            PlayPingPongThread pong =
                new PlayPingPongThread("Pong!");
            
            // Start ping and pong threads, which calls their run()
            // methods.
            ping.start();
            pong.start();

            // Wait for both threads to exit before exiting main().
            ping.join();
            pong.join();
            
            System.out.println("Done!");
        } 
        catch (java.lang.InterruptedException e)
            {}
    }
}
