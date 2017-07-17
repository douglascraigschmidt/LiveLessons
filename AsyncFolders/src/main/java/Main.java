import folder.Dirent;
import folder.EntryVisitor;
import folder.Folder;
import utils.Options;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example shows ...
 */
public class Main {
    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_FOLDER =
        "works";

    static void display(String string) {
        if (Options.getInstance().isVerbose())
            System.out.println(string);
    }

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv)
            throws IOException, URISyntaxException {
        Options.getInstance().parseArgs(argv);

        long startTime = System.nanoTime();

        EntryVisitor printVisitor = EntryVisitor
            .of((folder)
                -> display("folder name = "
                           + folder.getName()),
                (document) 
                -> display("document name = " 
                           + document.getName()));

        CompletableFuture<Dirent> folderFuture = Folder
            // Asynchronously create a Folder containing the complete
            // works of Shakespeare.
            .fromDirectory(TestDataFactory
                           .getRootFolderFile(sSHAKESPEARE_FOLDER),
                           // Create a visitor that prints out the
                           // contents of the folder as it's being
                           // created asynchronously.
                           printVisitor,
                           Options.getInstance().isParallel());
                           
        Dirent rootFolder = folderFuture
            // After the Folder is created count the number of entries
            // in it.
            .thenApply(folder
                        -> {
                           Stream<Dirent> folderStream = folder
                           .stream();

                           if (Options.getInstance().isParallel())
                               folderStream.parallel();

                           System.out.println("number of entries in the folder = "
                                   + folderStream.count());
                           return folder;
                       })
            // Wait for all the processing to complete.
            .join();

        // Search for a word in the folder.
        searchFolder(rootFolder, "anon");

        System.out.println((Options.getInstance().isParallel() ? "Parallel" : "Sequential")
                           + " test ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");
    }

    private static void searchFolder(Dirent rootFolder,
                                     String searchedWord) {
        EntryVisitor searchVisitor = EntryVisitor
            .of((folder)
                -> {
                },
                (document) 
                -> {
                    long occurrences = occurrencesCount(document.getContents(),
                                                       searchedWord);

                    if (occurrences > 0)
                        System.out.println("\"anon\" occurs "
                                           + occurrences
                                           + " time(s) in document "
                                           + document.getPath());
                });

        Stream<Dirent> folderStream = rootFolder
                .stream();

        if (Options.getInstance().isParallel())
            folderStream.parallel();

        folderStream
            .forEach(dirent
                     -> dirent.accept(searchVisitor));
    }

    private static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }
    
    private static Long occurrencesCount(CharSequence document,
                                         String searchedWord) {
        Stream<String> wordStream = Stream
            .of(wordsIn(document.toString()));

        if (Options.getInstance().isParallel())
            wordStream.parallel();

        return wordStream
            .filter(word -> searchedWord.equals(word))
            .count();
    }

}
