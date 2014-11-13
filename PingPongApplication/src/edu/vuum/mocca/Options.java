package edu.vuum.mocca;

import java.io.File;
import java.nio.*;
import java.util.*;

/**
 * @class Options
 * 
 * @brief This class implements the Singleton pattern to handle
 *        command-line option processing.
 */
public class Options
{
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /** 
     * Maximum number of iterations to run the program (defaults to
     * 10).
     */
    private int mMaxIterations = 6;

    /** Maximum number of iterations per "turn" (defaults to 1). */
    private int mMaxTurns = 1;

    /**
     * Which synchronization to use, e.g., "SEMA", "COND", "MONOBJ",
     * "QUEUE", and "PADDLE".  Defaults to "PADDLE".
     */
    private String mSyncMechanism = "PADDLE";

    /** Method to return the one and only singleton uniqueInstance. */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /** Number of iterations to run the program. */
    public int maxIterations() {
        return mMaxIterations;
    }

    /** Number of iterations to run the program. */
    public int maxTurns() {
        return mMaxTurns;
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
                else if (argv[argc].equals("-t"))
                    mMaxTurns = Integer.parseInt(argv[argc + 1]);
                else
                    {
                        printUsage();
                        return false;
                    }
            return true;
        } else
            return false;
    }

    /** Print out usage and default values. */
    public void printUsage() {
        PlatformStrategy platform = PlatformStrategy.instance();
        platform.errorLog("Options",
                          "\nHelp Invoked on ");
        platform.errorLog("Options",
                          "[-hist] ");
        platform.errorLog("", "");
        platform.errorLog("", "");

        platform.errorLog("Options",
                          "Usage: ");
        platform.errorLog("Options",
                          "-h: invoke help ");
        platform.errorLog("Options",
                          "-i max-number-of-iterations ");
        platform.errorLog("Options",
                          "-s sync-mechanism (e.g., \"SEMA\", \"COND\", \"MONOBJ\", \"QUEUE\", or \"BALL\"");
        platform.errorLog("Options",
                          "-t max-number-of-turns");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
