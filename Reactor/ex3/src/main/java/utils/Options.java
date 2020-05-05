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
     * Controls whether backpressure is enabled (defaults to true).
     */
    private boolean mBackPressureEnabled = true;

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
     * True if the producer and consumer should run in parallel, else
     * false.
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
     * @return True if debugging output is printed, else false.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * @return True if backpressure is enabled, else false.
     */
    public boolean backPressureEnabled() {
        return mBackPressureEnabled;
    }

    /**
     * @return True the producer and consumer should run in parallel,
     * else false.
     */
    public boolean parallel() {
        return mParallel;
    }

    /**
     * @return The number of integers to generate.
     */
    public int count() {
        return mCount;
    }

    /**
     * @return The max value for the random numbers.
     */
    public int maxValue() {
        return mMaxValue;
    }

    /**
     * @return True if logging is enabled, else false.
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
                case "-b":
                    mBackPressureEnabled = argv[argc + 1].equals("true");
                    break;
                case "-d":
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    break;
                case "-l":
                    mLoggingEnabled = argv[argc + 1].equals("true");
                        break;
                case "-c":
                    mCount = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-m":
                    mMaxValue = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-p":
                    mParallel = argv[argc + 1].equals("true");
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
        System.out.println("-b [true|false]"
                           + "-c [n] "
                           + "-d [true|false] "
                           + "-l [true|false] "
                           + "-m [maxValue] "
                           + "-p [true|false]");
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
