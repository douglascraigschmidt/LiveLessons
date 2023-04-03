package common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

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
     * The iteration when a diagnostic should be printed.
     */
    private int mPrintDiagnosticOnIteration = 10;

    /**
     * Controls how many longs are generated.
     */
    private int mCount = 100;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

    /**
     * The tags to use to control how {@code Options.debug()} behaves.
     */
    private List<String> mTagsList = new ArrayList<>();

    /**
     * Controls whether logging is enabled
     */
    private boolean mLoggingEnabled;

    /**
     * Count the # of pending items between publisher and subscriber.
     */
    public static final AtomicInteger sPendingItemCount =
        new AtomicInteger(0);

    /**
     * True if the producer and consumer should run in parallel, else
     * false.
     */
    private boolean mParallel = true;

    /**
     * The parallelism level if mParallel is true.  Defaults to 1.
     */
    private int mParallelism = 1;

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
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * @return True if {@code i} modulus the print diagnostic == 0,
     *         else false
     */
    public boolean printDiagnostic(int i) {
        return mDiagnosticsEnabled
                && (i % mPrintDiagnosticOnIteration) == 0;
    }

    /**
     * @return True the producer and consumer should run in parallel,
     * else false.
     */
    public boolean parallel() {
        return mParallel;
    }

    /**
     * @return The parallelism level.
     */
    public int parallelism() {
        return mParallelism;
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
        if (sInstance.mDiagnosticsEnabled
            && sInstance.mTagsList.contains(tag))
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
                    case "-d" -> mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    case "-l" -> mLoggingEnabled = argv[argc + 1].equals("true");
                    case "-c" -> mCount = Integer.parseInt(argv[argc + 1]);
                    case "-i" -> mPrintDiagnosticOnIteration = Integer.parseInt(argv[argc + 1]);
                    case "-m" -> mMaxValue = Integer.parseInt(argv[argc + 1]);
                    case "-p" -> mParallel = argv[argc + 1].equals("true");
                    case "-P" -> mParallelism = Integer.parseInt(argv[argc + 1]);
                    case "-T" -> mTagsList = Pattern
                            .compile(",")
                            .splitAsStream(argv[argc + 1])
                            .collect(toList());
                    default -> {
                        printUsage();
                        return;
                    }
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
        System.out.println("""
            -c [n]
            -d [true|false]
            -i [iteration]
            -l [true|false]
            -m [maxValue]
            -p [true|false]
            -P [parallelism]
            -T [tag,...]""");
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
