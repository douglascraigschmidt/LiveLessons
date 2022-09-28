package folders;

import folders.client.FolderClient;
import folders.server.FolderApplication;
import folders.common.Options;
import folders.utils.RunTimer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This example shows the use of Spring WebMVC to apply Java
 * sequential and parallel streams to process entries in a
 * recursively-structured directory folder sequentially and/or
 * concurrently in a client/server environment.  This example also
 * shows how to encode/decode complex objects that use inheritance
 * relationships and transmits them between processes.
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
     * This object connects {@link FolderApplicationTest} to the
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
            "true" 
    };

    /**
     * Run all the tests and print the timing results.
     */
    @Test
    public void runTests() {
        System.out.println("Starting WebMVCFolders test");

        Options.getInstance().parseArgs(mArgv);

        if (Options.getInstance().sequential())
            // Run the mostly local tests sequentially.
            mFolderClient.runTests(false);

        if (Options.getInstance().concurrent())
            // Run the mostly local tests concurrently.
            mFolderClient.runTests(true);

        if (Options.getInstance().sequential())
            // Run the remote tests sequentially.
            mFolderClient.runRemoteTests(false);

        if (Options.getInstance().concurrent())
            // Run the remote tests concurrently.
            mFolderClient.runRemoteTests(true);

        // Print results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending WebMVCFolders test");
    }
}
