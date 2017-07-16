import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods for obtaining test data.
 */
public class TestDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * Return a File object that's used to search a recursive
     * directory containing the complete works of William Shakespeare.
     */
    public static File getRootFolderFile(String rootFolderName)
        throws URISyntaxException, IOException {
        return new File(ClassLoader
                        .getSystemResource(rootFolderName)
                        .toURI());
    }

    /**
     * Return a folder object that's used to search a recursive
     * directory containing the complete works of William Shakespeare.
     */
    public static Folder getRootFolder(String rootFolderName,
                                        boolean parallel)
        throws URISyntaxException, IOException {
        return Folder
            .fromDirectory(getRootFolderFile(rootFolderName)
                           .toPath(),
                           parallel);
    }
}
