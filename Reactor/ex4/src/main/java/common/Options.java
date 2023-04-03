package common;

import reactor.core.publisher.FluxSink;
import utils.Memoizer;
import utils.TimedMemoizer;
import utils.TimedMemoizerEx;
import utils.UntimedMemoizer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
     * The tags to use to control how {@code Options.debug()} behaves.
     */
    private List<String> mTagsList = new ArrayList<>();

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
     * The parallelism level if mParallel is true.  Defaults to 1.
     */
    private int mParallelism = 1;

    /**
     * Keeps track of the Memoizer strategy.
     * 'U' - UntimedMemoizer
     * 'T' - TimedMemoizer
     * 'X' - TimedMemoizerEx
     */
    private char mMemoizerStrategy = 'U';

    /**
     * Timeout period (in milliseconds) for the timed memoizers.
     * Defaults to 2 seconds.
     */
    private int mTimeout = 2_000;

    /**
     * Keeps track of the OverflowStrategy.
     * 'D' - DROP (Drop the incoming signal if the downstream is not ready to receive it)
     * 'B' - BUFFER (Buffer all signals if the downstream can't keep up)
     * 'E' - ERROR (Signal an IllegalStateException when the downstream can't keep up)
     * 'I' - IGNORE (Completely ignore downstream backpressure requests)
     * 'L' - LATEST (Downstream will get only the latest signals from upstream)
     */
    private char mOverflowStrategy = 'B';

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
     * @return Return the overflow strategy.
     */
    public FluxSink.OverflowStrategy overflowStrategy() {
        return switch (mOverflowStrategy) {
            case 'D' -> FluxSink.OverflowStrategy.DROP;
            case 'B' -> FluxSink.OverflowStrategy.BUFFER;
            case 'E' -> FluxSink.OverflowStrategy.ERROR;
            case 'I' -> FluxSink.OverflowStrategy.IGNORE;
            case 'L' -> FluxSink.OverflowStrategy.LATEST;
            default -> throw new IllegalArgumentException("invalid OverflowStrategy");
        };
    }

    /**
     * @return The number of integers to generate.
     */
    public int count() {
        return mCount;
    }

    /**
     * @return Return the timeout.
     */
    public long timeout() {
        return mTimeout;
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
     * Make the requested memoizer.
     */
    public static Memoizer<Integer, Integer> makeMemoizer
        (Function<Integer, Integer> function) {
        return switch (instance().mMemoizerStrategy) {
            case 'U' -> new UntimedMemoizer<>(function);
            case 'T' -> new TimedMemoizer<>
                    (function,
                            instance().mTimeout);
            case 'X' -> new TimedMemoizerEx<>
                    (function,
                            instance().mTimeout);
            default -> throw new IllegalArgumentException("given memoizer type unknown");
        };
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
                    case "-m" -> mMaxValue = Integer.parseInt(argv[argc + 1]);
                    case "-M" -> mMemoizerStrategy = argv[argc + 1].charAt(0);
                    case "-o" -> mOverflowStrategy = argv[argc + 1].charAt(0);
                    case "-p" -> mParallel = argv[argc + 1].equals("true");
                    case "-P" -> mParallelism = Integer.parseInt(argv[argc + 1]);
                    case "-t" -> mTimeout = Integer.parseInt(argv[argc + 1]);
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
                           + "-M [T|U|X]"
                           + "-o [B|D|E|I|L]"
                           + "-p [true|false]"
                           + "-P [parallelism]"
                           + "-t [timeoutInMillis]"
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
