import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

/**
 * This example shows ...
 */
public class ex19 {
    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_FOLDER =
        "works";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv)
            throws IOException, URISyntaxException {

        Folder folder = Folder
            .fromDirectory(TestDataFactory
                           .getRootFolderFile(sSHAKESPEARE_FOLDER)
                           .toPath(),
                           true);

        System.out.println("number of entires in the folder = "
                           + countFolder(folder));
    }

    static long countFolder(Folder folder) {
        long numSubFolders = folder
            .getSubFolders()
            .parallelStream()
            .mapToLong(ex19::countFolder)
            .sum();

        long numDocuments = folder
            .getDocuments()
            .parallelStream()
            .count();
        
        return numDocuments + numSubFolders;
    }
}
