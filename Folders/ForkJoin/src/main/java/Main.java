import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * This example shows how to use the Java fork-join pool framework and
 * Java sequential streams to count the number of files in a (large)
 * recursive folder hierarchy, as well as calculate the cumulative
 * sizes of all the files.
 *
 * Interestingly, earlier versions of Java (e.g., Java 8) seem to have
 * a "quirk" with the common fork-join pool where it will continue to
 * grow without bound when used with blocking I/O calls.  This doesn't
 * seem to be a problem with later versions of Java, e.g., Java 11.
 */
class Main {
    /**
     * The fork-join pool to use for the program.
     */
    private static final ForkJoinPool sFJPool =
        ForkJoinPool.commonPool();
    // @@ If you change the initialization of sFJPool to use the
    // common pool you may get a runtime error with earlier versions
    // of Java.  In that case, I recommend using new ForkJoinPool().

    /**
     * Main entry point into the program runs the tests.
     */
    public static void main(String[] args) throws URISyntaxException {
        System.out.println("Starting the file counter program");

        // Run the GC first.
        System.gc();

        // Take a snapshot of the current time.
        long start = System.currentTimeMillis();

        // Create a task that will count the number of files and sizes
        // of the files in a large directory hierarchy.
        FileCounterTask fileCounterTask = new FileCounterTask
            (new File(ClassLoader.getSystemResource("works").toURI()));

        // Run the FileCounterTask on the root of a large directory
        // hierarchy.
        long size = sFJPool.invoke(fileCounterTask);

        // Print the results.
        System.out.println(""
                           + (fileCounterTask.documentCount()
                              + fileCounterTask.folderCount())
                           + " files ("
                           + fileCounterTask.documentCount()
                           + " documents and " +
                           + fileCounterTask.folderCount()
                           + " folders) contained "
                           + size // / 1_000_000)
                           + " bytes");
        System.out.println("total time taken for the processing was "
                           + (System.currentTimeMillis() - start)
                           + " ms");
        System.out.println();
        System.out.println("pool size = " + sFJPool.getPoolSize() +
                           ", steal count = " + sFJPool.getStealCount() +
                           ", running thread count = " + sFJPool.getRunningThreadCount());

        System.out.println("Ending the file counter program");
    }
}

