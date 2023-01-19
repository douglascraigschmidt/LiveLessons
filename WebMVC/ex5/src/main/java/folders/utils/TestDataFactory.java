package folders.utils;

import folders.common.InvalidFolderException;

import java.io.File;
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
        var url = ClassLoader
                .getSystemResource(rootFolderName);

        if (url == null)
            throw new InvalidFolderException(rootFolderName);

        Function<String, File> getFile = ExceptionUtils
                // An adapter that simplifies checked exceptions.
                .rethrowFunction(name ->
                                 new File(url
                                          .toURI()));

        // Open and return the file.
        return getFile.apply(rootFolderName);
    }
}
