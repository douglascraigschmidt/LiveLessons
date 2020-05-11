package utils;

import ch.qos.logback.classic.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An options singleton.
 */
public class Options {
    /**
     * The one and only singleton unique instance.
     */
    private static Options sInstance;
    
    /**
     * Return the one and only singleton unique instance.
     */
    public static Options getInstance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * Stores whether the user wants verbose output or not.  Default
     * is no verbose output.
     */
    private boolean mVerbose = true;

    /**
     * A singleton should have a private constructor.
     */
    private Options() {
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] args) {
        // Disable the verbose/annoying Spring "debug" logging.
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));

        // Process the arguments.
        for (String arg : args) {
            if (arg.equals("-v"))
                mVerbose = true;
            else {
                printUsage();
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the mVerbose field to the value of @a verbose.
     */
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }

    /**
     * @return True if the user wants "verbose" output, else false
     */
    public boolean getVerbose() {
        return mVerbose;
    }

    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        System.out.println("Options usage: \n-v");
    }

    /**
     * Display {@code string} if the program is run in verbose mode.
     */
    public static void display(String string) {
        if (getInstance().getVerbose())
            System.out.println("["
                    + Thread.currentThread().getId()
                    + "] "
                    + string);
    }
}
