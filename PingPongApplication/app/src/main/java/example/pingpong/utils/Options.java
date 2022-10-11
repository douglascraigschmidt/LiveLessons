package example.pingpong.utils;

import example.pingpong.platform.PlatformStrategy;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /** 
     * Maximum number of iterations to run the program (defaults to
     * 10).
     */
    private int mMaxIterations = 6;

    /**
     * Which synchronization to use, e.g., "SEMA", "COND", "MONOBJ",
     * and "QUEUE".  Defaults to "SEMA".
     */
    private String mSyncMechanism = "SEMA";

    /** 
     * Method to return the one and only singleton uniqueInstance. 
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /** 
     * Number of iterations to run the program. 
     */
    public int maxIterations() {
        return mMaxIterations;
    }

    /**
     * Which synchronization to use, e.g., "SEMA", "COND", "MONOBJ",
     * "QUEUE", and "PADDLE".  Defaults to "PADDLE".
     */
    public String syncMechanism() {
        return mSyncMechanism;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String argv[]) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                if (argv[argc].equals("-i"))
                    mMaxIterations = Integer.parseInt(argv[argc + 1]);
                else if (argv[argc].equals("-s"))
                    mSyncMechanism = argv[argc + 1];
                else {
                    printUsage();
                    return false;
                }
            return true;
        } else
            return false;
    }

    /** 
     * Print out usage and default values. 
     */
    public void printUsage() {
        PlatformStrategy platform = PlatformStrategy.instance();
        platform.errorLog("Options",
                          "\nHelp Invoked on ");
        platform.errorLog("Options",
                          "[-his] ");
        platform.errorLog("", "");
        platform.errorLog("", "");

        platform.errorLog("Options",
                          "Usage: ");
        platform.errorLog("Options",
                          "-h: invoke help");
        platform.errorLog("Options",
                          "-i max-number-of-iterations");
        platform.errorLog("Options",
                          "-s sync-mechanism (e.g., \"SEMA\", \"COND\", \"MONOBJ\", or \"QUEUE\"");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
