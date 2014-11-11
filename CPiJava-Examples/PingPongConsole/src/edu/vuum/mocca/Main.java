package edu.vuum.mocca;

/**
 * @class Main
 *
 * @brief This class is the main entry point for the command-line
 *        version of the PlayPingPong app.
 */
public class Main  {
    /**
     * The Java virtual machine requires the instantiation of a main
     * method to run the console version of the PlayPingPong app.
     */
    public static void main(String[] args) {
        /** Initializes the Options singleton. */
        Options.instance().parseArgs(args);

        /**
         * Create a PlayPingPong object to run the designated number of
         * iterations.
         */
        PlayPingPong playPingPong =
            PlayPingPong.makePlayPingPong(Options.instance().maxIterations());

        /**
         * Play ping-pong.
         */
        playPingPong.run();
    }
}
