package expressiontree.utils;

import expressiontree.platspecs.Platform;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /** 
     * Are we running in verbose mode or not? 
     */
    private boolean verbose = false;

    /**
     * The singleton @a Options instance. 
     */
    private static Options uniqueInstance = null;

    /** 
     * Method to return the one and only singleton uniqueInstance. 
     */
    public static Options instance() {
        if (uniqueInstance == null)
            uniqueInstance = new Options();

        return uniqueInstance;
    }

    /** 
     * Run the program in verbose mode. 
     */
    public boolean verbose() {
        return this.verbose;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String argv[]) {
        if (argv.length > 0) {
            if (argv[0].equals("-v"))
                verbose = true;
            else {
                printUsage();
                return false;
            }
        }

        return true;
    }

    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        Platform platform = Platform.instance();
        platform.errorLog("Options",
                          "\nHelp Invoked on ");
        platform.errorLog("Options",
                          "[-h|-v] ");
        platform.errorLog("",
                          "");
        platform.errorLog("",
                          "");

        platform.errorLog("Options",
                          "Usage: ");
        platform.errorLog("Options",
                          "-h: invoke help ");
        platform.errorLog("Options",
                          "-v: enter verbose mode \n");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
