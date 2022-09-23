package vandy.mooc.pingpong.utils;

import android.util.Log;

import java.io.File;
import java.nio.*;
import java.util.*;

/**
 * This class implements the Singleton pattern to handle
 * command-line option processing.
 */
public class Options {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /** 
     * Maximum number of iterations to run the program (defaults to
     * 100).
     */
    private int mMaxIterations = 100;

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
                switch (argv[argc]) {
                    case "-i":
                        mMaxIterations = Integer.parseInt(argv[argc + 1]);
                        break;
                    case "-s":
                        mSyncMechanism = argv[argc + 1];
                        break;
                    default:
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
        Log.d(TAG,
              "\nHelp Invoked on ");
        Log.d(TAG,
              "[-his] ");
        Log.d("", "");
        Log.d("", "");

        Log.d(TAG,
              "Usage: ");
        Log.d(TAG,
              "-h: invoke help");
        Log.d(TAG,
              "-i max-number-of-iterations");
        Log.d(TAG,
              "-s sync-mechanism (e.g., \"SEMA\", \"COND\", or \"MONOBJ\"");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
