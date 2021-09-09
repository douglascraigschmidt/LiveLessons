package utils;

import java.util.Map;
import java.util.function.Function;

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
     * The singleton @a Options instance. 
     */
    private static Options mUniqueInstance = null;

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Controls how many longs are generated.
     */
    private int mCount = 500;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

    /**
     * Controls the max tries for optimistic reads.
     */
    private int mMaxTries = 2;

    /**
     * Controls whether logging is enabled
     */
    private boolean mLoggingEnabled;

    /**
     * Keeps track of the StampedLock strategy.
     * 'W' - write lock
     * 'O' - optimistic read
     * 'C' - conditional write
     */
    private char mStampedLockStrategy = 'W';

    /**
     * Keeps track of whether to run the tests using a sequential or
     * parallel stream.
     */
    private boolean mParallel = true;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Returns whether debugging output is generated.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Returns whether to run the stream in parallel or not.
     */
    public boolean parallel() {
        return mParallel;
    }

    /**
     * Returns the number of integers to generate.
     */
    public int count() {
        return mCount;
    }

    /**
     * Returns the max value
     */
    public int maxValue() {
        return mMaxValue;
    }

    /**
     * Returns the max number of tries for the optimistic read.
     */
    public int maxTries() {
        return mMaxTries;
    }

    /**
     * Returns the StampedLock strategy.
     */
    public char stampedLockStrategy() {
        return mStampedLockStrategy;
    }

    /**
     * Returns whether logging is enabled or not.
     */
    public boolean loggingEnabled() {
        return mLoggingEnabled;
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
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String string) {
        if (mUniqueInstance.mDiagnosticsEnabled)
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
                case "-l":
                    mLoggingEnabled = argv[argc + 1].equals("true");
                        break;
                case "-c":
                    mCount = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-s":
                    mStampedLockStrategy = argv[argc + 1].charAt(0);
                    break;
                case "-m":
                    mMaxValue = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-p":
                    mParallel = argv[argc + 1].equals("true");
                    break;
                case "-t":
                    mMaxTries = Integer.parseInt(argv[argc + 1]);
                    break;
                default:
                    printUsage();
                    return;
                }
            if (mMaxValue - mCount <= 0)
                throw new IllegalArgumentException("maxValue - count must be greater than 0");
        }
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-c [n] "
                           + "-d [true|false] "
                           + "-l [true|false] "
                           + "-m [maxValue] "
                           + "-p [true|false]"
                           + "-s [W|C|O] "
                           + "-t [maxTries]");
    }

    /**
     * Print the {@code element} and the {@code operation} along with
     * the current thread name to aid debugging and comprehension.
     *
     * @param element The given element
     * @param operation The Reactor operation being performed
     * @return The element parameter
     */
    public static <T> T logIdentity(T element, String operation) {
        System.out.println("["
                           + Thread.currentThread().getName()
                           + "] "
                           + operation
                           + " -- " 
                           + element);
        return element;
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
