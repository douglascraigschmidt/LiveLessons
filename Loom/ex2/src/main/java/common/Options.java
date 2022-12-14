package common;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /** 
     * The singleton {@link Options} instance. 
     */
    private static Options mUniqueInstance = null;

    /**
     * Number of random Integer objects to create.
     */
    private int mNumberOfElements = 20;

    /**
     * Create virtual threads if true, otherwise create platform
     * threads if false.
     */
    private boolean mVirtualThreads = true;

    /**
     * @return The one and only singleton uniqueInstance
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * @return Number of random {@link Integer} objects to create
     */
    public int numberOfElements() {
        return mNumberOfElements;
    }

    /**
     * @return True if virtual threads requested, otherwise platform
     *         threads
     */
    public boolean virtualThreads() {
        return mVirtualThreads;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                case "-d":
                    break;
                case "-n":
                    mNumberOfElements = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-t":
                    mVirtualThreads = argv[argc + 1].equals("v");
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
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-n [numberOfElements]");
        System.out.println("-t [p|v]");
    }

    /**
     * Display {@code message} after printing thread id.
     * @param message The message to display
     */
    public static void display(String message) {
        System.out.println("Thread = "
                + Thread.currentThread().getId()
                + " "
                + message);
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
