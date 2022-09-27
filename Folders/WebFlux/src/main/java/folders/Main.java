package folders;

import folders.client.FolderClient;
import folders.utils.Options;
import folders.utils.RunTimer;

/**
 * This example shows the use of a Spring WebFlux micro-service to
 * apply reactive streams top-to-bottom and end-to-end to process
 * entries in a recursively-structured directory folder sequentially,
 * concurrently, and in parallel in a distributed environment.  This
 * example also encodes/decodes complex objects that use inheritance
 * relationships and transmits them between processes.
 */
public class Main {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        System.out.println("Starting WebFluxFolders test");

        // Parse the options.
        Options.getInstance().parseArgs(argv);

        if (Options.getInstance().sequential()
            && Options.getInstance().local())
            // Run the tests sequentially and locally.
            FolderClient.runTests(false, false);

        if (Options.getInstance().sequential()
            && Options.getInstance().remote())
            // Run the tests sequentially and remotely.
            FolderClient.runTests(false, true);

        if (Options.getInstance().concurrent()
            && Options.getInstance().local())
            // Run the tests concurrently and locally.
            FolderClient.runTests(true, false);

        if (Options.getInstance().concurrent()
            && Options.getInstance().remote())
            // Run the tests concurrently and remotely.
            FolderClient.runTests(true, true);

        if (Options.getInstance().parallel())
            // Run the tests in parallel.
            FolderClient.runTestsParallel();
        
        if (Options.getInstance().remote())
            FolderClient.runRemoteTests();

        // Print results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending WebFluxFolders test");
    }
}
