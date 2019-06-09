import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows how to use the Java fork-join pool framework and
 * Java 8 sequential streams to count the number of files in a (large)
 * recursive folder hierarchy, as well as calculate the cumulative
 * sizes of all the files.  Note that all the files have size 0, so
 * the total result is 0!
 *
 * Interestingly, earlier versions of Java (e.g., Java 8) seem to have
 * a "quirk" with the common fork-join pool where it will continue to
 * grow without bound when used with blocking I/O calls.  This doesn't
 * seem to be a problem with later versions of Java, e.g., Java 12.
 */
class FileCounter {
    /**
     * The fork-join pool to use for the program.
     */
    private static ForkJoinPool sFJPool =
        ForkJoinPool.commonPool();
    // @@ If you change the initialization of sFJPool to use the
    // common pool you may get a runtime error with earlier versions
    // of Java.  In that case, I recommmend using new ForkJoinPool().

    /**
     * Keeps track of the total number of files encountered.
     */
    static private AtomicLong sFileCount = new AtomicLong(0);

    /**
     * This taask computes the size in bytes of the file (or all the
     * files associated with subdirectory).
     */
    static class FileTask extends RecursiveTask<Long> {
        /**
         * The current file that's being analyzed.
         */
        private File mFile;

        /**
         * Constructor initializes the file.
         */
	FileTask(File file) {
            mFile = file;
	}
	
        /**
         * This hook method returns the size in bytes of the file (or
         * all the files associated with subdirectory).
         */
	@Override
	protected Long compute() {
            // Determine if mFile is a file (vs. a directory).
            if (mFile.isFile()) {
                // Increment the count of files.
                sFileCount.incrementAndGet();

                // Return the length of the file.
                return mFile.length();
            } else {
                // Create a list of tasks to fork.
                List<ForkJoinTask<Long>> forks = Stream
                    // Convert the list of files into a stream of files.
                    .of(Objects.requireNonNull(mFile.listFiles()))

                    // Map each file into a FileTask and fork it.
                    .map(temp -> new FileTask(temp).fork())

                    // Trigger intermediate operation processing and
                    // collect the results into a list.
                    .collect(toList());

                return forks
                    // Convert the list to a stream.
                    .stream()
                    
                    // Join the tasks.
                    .mapToLong(ForkJoinTask::join)
                    
                    // Increment the number of files encountered.
                    .peek(unused -> sFileCount.incrementAndGet())

                    // Sum the sizes of all the files.
                    .sum();
            }
	}
    }

    /**
     * Main entry point into the program runs the tests.
     */
    public static void main(String[] args) throws URISyntaxException {
        // Run the GC first.
        System.gc();

        // Initialize the count to 0.
        sFileCount.set(0);

        // Take a snapshot of the current time.
        long start = System.currentTimeMillis();

        // Run the FileTask on the root of the (large) directory
        // hierarchy.
        long size = sFJPool
            .invoke(new FileTask
                    (new File(ClassLoader.getSystemResource("works").toURI())));

        // Print the results.
        System.out.println("total time to process "
                           + sFileCount.get()
                           + " files was " + (System.currentTimeMillis() - start)
                           + " ms");
        System.out.println((size/1000000) + "MB");
        System.out.println("pool size = " + sFJPool.getPoolSize() +
                           ", steal count = " + sFJPool.getStealCount() +
                           ", running thread count = " + sFJPool.getRunningThreadCount());
    }
}

