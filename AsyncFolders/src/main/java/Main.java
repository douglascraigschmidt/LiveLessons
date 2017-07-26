import folder.Dirent;
import folder.Document;
import folder.EntryVisitor;
import folder.Folder;
import utils.Options;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example shows the use of the Java 8 streams and completable
 * futures frameworks to process entries in a recursively structured
 * directory folder in parallel and/or asynchronously.
 */
public class Main {
    /**
     * The input "works".
     */
    private static final String sWORKs =
        "works";

    /**
     * Display @a string if the program is run in verbose mode.
     */
    static void display(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println(string);
    }

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv)
            throws IOException, URISyntaxException {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Start the timer.
        long startTime = System.nanoTime();

        // Create a visitor that's used during the creation of the
        // root folder.
        EntryVisitor printVisitor = EntryVisitor
            .of((folder)
                -> display("folder name = "
                           + folder.getName()),
                (document)
                -> display("document name = "
                           + document.getName()));

        CompletableFuture<Dirent> folderFuture = Folder
            // Asynchronously create a folder containing all the
            // works.
            .fromDirectory(TestDataFactory
                           .getRootFolderFile(sWORKs),
                           // Create a visitor that prints out the
                           // contents of the folder as it's being
                           // created asynchronously.
                           printVisitor,
                           Options.getInstance().getParallel());

        Dirent rootFolder = folderFuture
            // After the folder is created count the number of entries
            // in it.
            .thenApply(folder
                       -> {
                           Stream<Dirent> folderStream = folder
                           .stream();

                           if (Options.getInstance().getParallel())
                               folderStream.parallel();

                           long count = folderStream
                           .count();

                           System.out.println("number of entries in the folder = "
                                              + count);
                           return folder;
                       })
            // Wait for all the processing to complete.
            .join();

        // Search for a word in the folder.
        searchFolderVisitor(rootFolder, "CompletableFuture");
        searchFolderStream(rootFolder, "CompletableFuture");

        // Count the number of lines in the folder.
        countLines(rootFolder);

        // Print the timing results.
        System.out.println((Options.getInstance().getParallel() ? "Parallel" : "Sequential")
                           + " test ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");
    }

    /**
     * Find all occurrences of @a searchedWord in @a rootFolder using
     * a visitor.
     */
    private static void searchFolderVisitor(Dirent rootFolder,
                                            String searchedWord) {
        // Options.getInstance().setVerbose(true);

        // Create a visitor that displays results.
        EntryVisitor searchVisitor = EntryVisitor
            .of((folder)
                -> {
                    display("in folder "
                            + folder.getName());
                },
                (document)
                -> {
                    // Find how many times searchedWord occurs in the
                    // document.
                    long occurrences = occurrencesCount(document.getContents(),
                                                        searchedWord);

                    // Display the results.
                    if (occurrences > 0)
                        display("\"" 
                                + searchedWord 
                                + "\" occurs "
                                + occurrences
                                + " time(s) in document "
                                + document.getName());
                });

        // Create a stream for the folder.
        Stream<Dirent> folderStream = rootFolder
            .stream();

        // Convert to the parallel stream if desired.
        if (Options.getInstance().getParallel())
            folderStream.parallel();

        // Apply searchVisitor on each dirent in the stream.
        folderStream
            .forEach(dirent
                     -> dirent.accept(searchVisitor));
    }

    /**
     * Find all occurrences of @a searchedWord in @a rootFolder using
     * a stream.
     */
    private static void searchFolderStream(Dirent rootFolder,
                                           String searchedWord) {
        // Create a stream for the folder.
        Stream<Dirent> folderStream = rootFolder
                .stream();

        // Convert to the parallel stream if desired.
        if (Options.getInstance().getParallel())
            folderStream.parallel();

        // Compute the total number of matches of searchedWord.
        long matches = folderStream
            // Only search documents.
            .filter(dirent
                    -> dirent instanceof Document)

            // Search the document.
            .mapToLong(document
                       -> occurrencesCount(document.getContents(),
                                           searchedWord))
            // Sum the results.
            .sum();

        // Print the results.
        System.out.println("total matches of \""
                + searchedWord
                + "\" = "
                + matches);

    }

    /**
     * Determine # of times @a searchedWord appears in @a document.
     */
    private static Long occurrencesCount(CharSequence document,
                                         String searchedWord) {
        Stream<String> wordStream = Pattern
                // Compile word splitter into a regular expression
                // (regex).
                .compile("\\W+")

                // Use the regex to split the file into a stream of
                // words.
                .splitAsStream(document);

        // Convert to the parallel stream if desired.
        if (Options.getInstance().getParallel())
            wordStream.parallel();

        // Return # of times searchedWord appears in the stream.
        return wordStream
            // Only consider words that match.
            .filter(word
                    -> searchedWord.equals(word))

            // Count the results.
            .count();
    }

    /**
     * Count # of lines in the recursively structured directory at @a
     * rootFolder.
     */
    private static void countLines(Dirent rootFolder) {
        // Options.getInstance().setVerbose(true);

        // Create a stream for the folder.
        Stream<Dirent> folderStream = rootFolder
                .stream();

        // Convert to the parallel stream if desired.
        if (Options.getInstance().getParallel())
            folderStream.parallel();

        // Count # of lines in documents residing in the folder.
        long lineCount = folderStream
            // Only consider documents. 
            .filter(dirent
                    -> dirent instanceof Document)

            // Count # of lines in the document.
            .mapToInt(document
                      -> document
                      // Get contents of document
                      .getContents().toString()
                      // Split document by newline.
                      .split("[\n\r]")
                      
                      // Return length of the result.
                      .length)

            // Sum the results;
            .sum());

        System.out.println("total number of lines = "
                           + lineCount);
    }
}
