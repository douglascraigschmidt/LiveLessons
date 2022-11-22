package common;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
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
     * Controls how many longs are generated.
     */
    private int mCount = 100;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

    /**
     * Controls the request size passed to {@link Subscription} {@code
     * request()}.
     */
    private int mRequestSize = Integer.MAX_VALUE;

    /**
     * The tags to use to control how {@code Options.debug()} behaves.
     */
    private List<String> mTagsList = new ArrayList<>();

    /**
     * Controls whether logging is enabled
     */
    private boolean mLoggingEnabled;

    /**
     * The interval to print output.
     */
    private int mPrintInterval = 100;

    /**
     * The {@link Scheduler} strategy.
     */
    private String mSchedulerStrategy = "single";

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
     * @return The request size passed to {@link Subscription} {@code
     *         request()}
     */
    public int requestSize() {
        return mRequestSize;
    }

    /**
     * @return True of this {@code iteration} should be printed, else false
     */
    public boolean printIteration(int iteration) {
        return (iteration % mPrintInterval) == 0;
    }

    /**
     * @return True if logging is enabled, else false
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
     * @return The given {@link Scheduler} strategy
     */
    public Scheduler scheduler() {
        return switch (mSchedulerStrategy) {
        case "single" -> Schedulers.single();
        case "computation" -> Schedulers.computation();
        default -> throw new IllegalArgumentException("invalid Scheduler Strategy");
        };
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
                    case "-m" -> mMaxValue = Integer.parseInt(argv[argc + 1]);
                    case "-p" -> mPrintInterval = Integer.parseInt(argv[argc + 1]);
                    case "-r" -> mRequestSize = Integer.parseInt(argv[argc + 1]);
                    case "-s" -> mSchedulerStrategy = argv[argc + 1];
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
        System.out.println("-c [n] "
                           + "-d [true|false] "
                           + "-l [true|false] "
                           + "-m [maxValue] "
                           + "-p [printInterval]"
                           + "-r [requestSize] "
                           + "-T [tag,...]");
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
