package folders.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Function;

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
     * directory containing test folder contents.
     */
    public static File getRootFolderFile(String rootFolderName){
        Function<String, File> getFile = ExceptionUtils
                // An adapter that simplifies checked exceptions.
                .rethrowFunction(name ->
                                 new File(ClassLoader
                                          .getSystemResource(name)
                                          .toURI()));

        // Open and return the file.
        return getFile.apply(rootFolderName);
    }
}
