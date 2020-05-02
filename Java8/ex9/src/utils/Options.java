package utils;

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
    private int mCount = 100;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

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
     * Display the string if diagnostics are enabled.
     */
    public static void display(String string) {
        if (mUniqueInstance.mDiagnosticsEnabled)
            System.out.println(string);
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
        System.out.println("-c [n] -d [true|false] -l [true|false] -m [maxValue] -s [W|C|O]");
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
