package edu.vandy.quoteservices.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /**
     * Logging tag.
     */
    private static final String TAG = Options.class.getName();

    /**
     * The singleton {@code Options} instance.
     */
    private static Options sInstance = null;

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDebugging = false;

    /**
     * Controls whether connections will be pooled.
     */
    private boolean mPoolConnections = true;

    /**
     * Controls how many random numbers are generated.
     */
    private int mNumberOfElements = 100;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * @return True if debugging output is printed, else false.
     */
    public boolean getDebug() {
        return mDebugging;
    }

    /**
     * @return The number of integers to generate.
     */
    public int quoteCount() {
        return mNumberOfElements;
    }

    /**
     * Set the number of integers to generate.
     */
    public void quoteCount(int count) {
        mNumberOfElements = count;
    }

    /**
     * @return True if connection pooling is enabled, else false.
     */
    public boolean poolConnections() {
        return mPoolConnections;
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String string) {
        if (sInstance.mDebugging)
            System.out.println("[" +
                               Thread.currentThread().getName()
                               + "] "
                               + string);
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String tag, String string) {
        if (sInstance.mDebugging)
            Options.debug(string);
    }

    /**
     * Print the string with thread information included.
     */
    public static void print(String string) {
        System.out.println("[" +
                           Thread.currentThread().getName()
                           + "] "
                           + string);
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                    case "-d" -> mDebugging = argv[argc + 1].equals("true");
                    case "-n" -> mNumberOfElements = Integer.parseInt(argv[argc + 1]);
                    case "-p" -> mPoolConnections = argv[argc + 1].equals("true");
                    default -> {
                        printUsage();
                        return;
                    }
                }

            var numberOfElements = System.getenv("COUNT");
            if (numberOfElements != null)
                quoteCount(Integer.parseInt(numberOfElements));

            var poolConnections = System.getenv("POOL_CONNECTIONS");
            if (poolConnections != null)
                mPoolConnections = poolConnections.equals("true");}
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]"
                           + "-n [n]"
                           + "-p [true|false]");
    }

    /**
     * Display {@code message} after printing thread id.
     * @param message The message to display
     */
    public static void display(String message) {
        System.out.println("Thread = "
                           + Thread.currentThread().threadId()
                           + " "
                           + message);
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
        // Disable the verbose/annoying Spring "debug" logging.
        Logger logger = (Logger)
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
