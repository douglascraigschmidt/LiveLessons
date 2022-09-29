package folder.folder;

import folders.client.FolderClient;
import folders.server.FolderApplication;
import folders.common.Options;
import folders.utils.RunTimer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This example shows the use of Spring WebFlux to apply Project
 * Reactor classes to process entries in a recursively-structured
 * directory folder sequentially and/or concurrently in a
 * client/server environment.  This example also shows how to
 * encode/decode complex objects that use inheritance relationships
 * and transmits them between processes.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link FolderApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = FolderApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FolderApplicationTest {
    /**
     * This object connects {@link FolderApplicationTests} to the
     * {@code FolderClient}.  The {@code @Autowired} annotation
     * ensures this field is initialized via Spring dependency
     * injection, where an object receives another object it depends
     * on (e.g., by creating a {@link FolderClient}).
     */
    @Autowired
    private FolderClient mFolderClient;

    /**
     * Emulate the "command-line" arguments for the tests.
     */
    private final String[] mArgv = new String[]{
            "-d",
            "true", 
            "-c",
            "true",
            "-s",
            "true",
            "-r",
            "true",
            "-l",
            "true"
    };

    /**
     * Run all the tests and print the timing results.
     */
    @Test
    public void runTests() {
        System.out.println("Starting WebFluxFolders test");

        Options.getInstance().parseArgs(mArgv);

        if (Options.getInstance().sequential()
            && Options.getInstance().local())
            // Run the tests sequentially and locally.
            mFolderClient.runTests(false, false);

        if (Options.getInstance().sequential()
            && Options.getInstance().remote())
            // Run the tests sequentially and remotely.
            mFolderClient.runTests(false, true);

        if (Options.getInstance().concurrent()
            && Options.getInstance().local())
            // Run the tests concurrently and locally.
            mFolderClient.runTests(true, false);

        if (Options.getInstance().concurrent()
            && Options.getInstance().remote())
            // Run the tests concurrently and remotely.
            mFolderClient.runTests(true, true);

        if (Options.getInstance().parallel())
            // Run the tests in parallel.
            mFolderClient.runTestsParallel();
        
        if (Options.getInstance().remote()
            && Options.getInstance().sequential())
            mFolderClient.runRemoteTests(false);

        if (Options.getInstance().remote()
            && Options.getInstance().parallel())
            mFolderClient.runRemoteTests(true);

        // Print results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending WebFluxFolders test");
    }
}
