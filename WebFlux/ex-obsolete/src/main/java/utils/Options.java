package utils;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.time.Duration;

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
    private boolean mDiagnosticsEnabled = false;

    /**
     * The maximum amount of time to wait for all the asynchronous
     * processing to complete.
     */
    private Duration mMaxTimeout = Duration.ofSeconds(5);

    /**
     * The maximum amount of time to wait for the ExchangeRate microservice to timeout.
     */
    private Duration mExchangeRateTimeout = Duration.ofSeconds(4);

    /**
     * The default exchange rate.
     */
    private double mDefaultRate = 1.0;

    /**
     * The number of iterations to run the test.
     */
    private int mMaxIterations = 5;

    /**
     *
     */
    private char mDefaultSync = 'c';

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * @return The max time to wait for computations to complete.
     */
    public Duration maxTimeout() {
        return mMaxTimeout;
    }

    /**
     * @return The time to wait for the ExchangeRate microservice to complete.
     */
    public Duration exchangeRateTimeout() {
        return mExchangeRateTimeout;
    }

    /**
     * @return The maximum numbers of iterations to run the tests.
     */
    public double maxIterations() {
        return mMaxIterations;
    }

    /**
     * @return The default rate.
     */
    public double defaultRate() {
        return mDefaultRate;
    }

    /**
     * @return True if debugging output is printed, else false.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String string) {
        if (sInstance.mDiagnosticsEnabled)
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
        if (sInstance.mDiagnosticsEnabled)
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
                    case "-d":
                        mDiagnosticsEnabled = argv[argc + 1].equals("true");
                        break;
                    case "-e":
                        mExchangeRateTimeout = Duration.ofSeconds(Integer.parseInt(argv[argc + 1]));
                        break;
                    case "-i":
                        mMaxIterations = Integer.parseInt(argv[argc + 1]);
                        break;
                    case "-m":
                        mMaxTimeout = Duration.ofSeconds(Integer.parseInt(argv[argc + 1]));
                        break;
                    case "-r":
                        mDefaultRate = Integer.parseInt(argv[argc + 1]);
                        break;
                    case "-s":
                        mDefaultSync = argv[argc + 1].charAt(0);
                        break;
                    default:
                        printUsage();
                        return;
                }
        }
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false] ");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
        // Disable the verbose/annoying Spring "debug" logging.
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));
    }

    /**
     * 
     * @return
     */
    public String getSyncTask() {
        switch (mDefaultSync) {
            case 's':
                return "sequential";
            case 't':
                return "threads";
            case 'e':
                return "executorService";
            case 'c':
                return "completionService";
            default:
                return "";
        }
    }
}
