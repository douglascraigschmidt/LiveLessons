package example.pingpong;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import example.pingpong.platform.PlatformStrategy;
import example.pingpong.platform.PlatformStrategyFactory;
import example.pingpong.threads.PlayPingPong;
import example.pingpong.utils.Options;

/**
 * Main Activity for the Android version of the PingPong app.
 */
public class MainActivity extends Activity {
    /** 
     * A plain TextView that PingPong will be "played" upon.
     */
    private TextView mPingPongTextViewLog;

    /** 
     * A more colorful TextView that prints "Ping" or "Pong" to the
     * display.
     */
    private TextView mPingPongColorOutput;

    /** 
     * Button that allows playing and resetting the concurrent
     * ping/pong algorithm.
     */
    private Button mPlayButton;
    
    /** 
     * Variables used to track state of the program.
     */
    private final static int RUN = 0;
    private final static int RESET = 1;
    private int mProgramState = RUN;

    /**
     * Hook method called when the Activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform platform
        // initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file, activity_ping_pong.
        setContentView(R.layout.activity_ping_pong);

        // Cache various TextView and Button widgets used to interact
        // with the user.
        mPingPongTextViewLog =
            (TextView) findViewById(R.id.pingpong_text_output);
        mPingPongColorOutput =
            (TextView) findViewById(R.id.pingpong_color_output);
        mPlayButton =
            (Button) findViewById(R.id.play_button);

        // Initializes the Platform singleton with the appropriate
        // Platform strategy, which in this case will be the
        // AndroidPlatform.
        PlatformStrategy.instance
            (new PlatformStrategyFactory(this)
             .makePlatformStrategy());

        // Initializes the Options singleton.
        Options.instance().parseArgs(null);
    }

    /** 
     * Sets the action of the button on click state. 
     */
    public void playButtonClicked(View view) {
        switch(mProgramState) {
        case RUN:
            // Create and start a background thread that uses the
            // Android HaMeR concurrency framework to run calls to
            // print() and done() on the UI Thread after a short
            // delay.
            mDelayedOutputThread = new DelayedOutputThread();
            mDelayedOutputThread.start();
        	
            // Use a factory method to create the appropriate type of
            // OutputStrategy.
            PlayPingPong pingPong =
                new PlayPingPong(Options.instance().maxIterations(),
                                 Options.instance().syncMechanism());

            // Play ping-pong with the designated number of
            // iterations.
            new Thread(pingPong).start();

            mPlayButton.setText(R.string.reset_button);
            mProgramState = RESET;
            break;
        case RESET:
            // Stop the thread that handles calls to print();
            mDelayedOutputThread.interrupt();
        	
            // Reset the color output.
            mPingPongColorOutput.setText("");
            mPingPongColorOutput.setBackgroundColor(Color.TRANSPARENT);
        	
            // Empty TextView and prepare the UI to start another run
            // of the concurrent ping/pong algorithm.
            mPingPongTextViewLog.setText(R.string.empty_string);
            mPlayButton.setText(R.string.play_button);
            mProgramState = RUN;
            break;
        default:
            // Notify the player that something has gone wrong and
            // reset.
            mPingPongTextViewLog.setText("Unknown State entered!");
            mProgramState = RESET;
        }
    }

    /** 
     * Instance of DelayedOutputThread that's described below.
     */
    private DelayedOutputThread mDelayedOutputThread;
    
    /*
     * @class DelayedOutputThread
     *
     * @brief Defines a HandlerThread that waits 0.5 seconds between
     *        handling messages so the "ping" and "pong" output is
     *        visually discernable by the user.
     */
    class DelayedOutputThread extends HandlerThread {
        /**
         * Handler that's used to post Runnables to the
         * HandlerThread's Looper.
         */
        public Handler mDelayedOutputHandler;

        /**
         * Constructor initializes the super class.
         */
        DelayedOutputThread() {
            super ("DelayedOutputThread");
        }
            
        /**
         * Hook method called back by HandlerThread.run() after the
         * Looper is initialized.
         */
        protected void onLooperPrepared() {
            // Create the Handler in the context of the
            // HandlerThread's Looper.
            mDelayedOutputHandler = new Handler();
        }

        /**
         * Run the specified command in the context of the
         * HandlerThread's Looper.
         */
        public void runOnDelayedOutputThread(Runnable command) {
            mDelayedOutputHandler.post(command);
        }
    }

    /**
     * Post a Runnable task that uses a CountDownLatch to indicate a
     * Thread has finished running.
     */
    public void done(final CountDownLatch exitBarrier) {
        // Post a Runnable task that decrements the CountDownLatch by
        // one.  This task's run() hook method will be dispatched
        // after all previous tasks ahead of it in the MessageQueue.
    	mDelayedOutputThread.runOnDelayedOutputThread(new Runnable() {
                public void run() {
                    exitBarrier.countDown();                        
                }
            });
    }

    /**
     * Prints the output string to the text log on screen. If the
     * string contains "ping" (case-insensitive) then a large Ping!
     * will be shown on screen with a certain color. The same goes for
     * strings containing "pong".
     * 
     * A call to this function will not block. However, the code to
     * display this output will be posted to a thread in such a way
     * that any changes to the UI will be spaced out by 0.5 seconds so
     * the user has an appropriate amount of time to appreciate the
     * ping'ing and the pong'ing that is happening.
     */
    public void print(final String output) {
        // Post a Runnable task that prints the output with a 0.5
        // second delay between displaying the output.
    	mDelayedOutputThread.runOnDelayedOutputThread(new Runnable() {
            @Override
            public void run() {
                // Post a Runnable whose run() method instructs the UI
                // to print the output.
                runOnUiThread(new Runnable() {	
                    @Override
                    public void run() {
                        mPingPongTextViewLog.append(output);
				        
                        // If we encounter a ping, throw it up on the
                        // screen in color.
                        if (output.toLowerCase(Locale.US).contains("ping")) {
                            mPingPongColorOutput.setBackgroundColor(Color.WHITE);
                            mPingPongColorOutput.setTextColor(Color.BLACK);
                            mPingPongColorOutput.setText("PING");
                        }
                        else if (output.toLowerCase(Locale.US).contains("pong")) {
                            mPingPongColorOutput.setBackgroundColor(Color.BLACK);
                            mPingPongColorOutput.setTextColor(Color.WHITE);
                            mPingPongColorOutput.setText("PONG");
                        }
                    }
                });
				
                // Wait 0.5 seconds before handling the next message.
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    // If we get interrupted, stop the looper
                    Looper.myLooper().quit();
                }
            }
        });
    }
}
