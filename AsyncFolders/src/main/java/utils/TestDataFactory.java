package utils;

import folder.Dirent;
import folder.EntryVisitor;
import folder.Folder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

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
    public static CompletableFuture<Dirent> getRootFolder(String rootFolderName,
                                                          EntryVisitor entryVisitor,
                                                          boolean parallel)
        throws URISyntaxException, IOException {
        return Folder
            .fromDirectory(getRootFolderFile(rootFolderName)
                           .toPath(),
                           entryVisitor,
                           parallel);
    }
}
