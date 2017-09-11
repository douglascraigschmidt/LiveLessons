import utils.BlockingTask;
import utils.Image;
import utils.NetUtils;
import utils.Options;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This example shows how to use the ManagedBlocker interface in the
 * Java fork-join pool to download multiple images from a remote
 * server.
 */
public class ex20 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex20.class.getName();

    /**
     * The JVM requires a static main() entry point to run the app.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        new ex20().run();
    }

    /**
     * Run the program.
     */
    private void run() {
        // Delete any the filtered images from the previous run.
        deleteDownloadedImages();

        // Get the list of files to the downloaded images.
        List<File> imageFiles = Options.instance().getUrlList()
            // Convert the URLs in the input list into a stream and
            // process them in parallel.
            .parallelStream()

            // Transform URL to a File by downloading each image via
            // its URL.  This call ensures the common fork/join thread
            // pool is expanded to handle the blocking image download.
            .map(this::downloadAndStoreImage)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(Collectors.toList());

        System.out.println(TAG + 
                           ": downloaded and stored "
                           + imageFiles.size()
                           + " images");
    }

    /**
     * Transform URL to a File by downloading each image via its
     * URL and storing it.
     */
    private File downloadAndStoreImage(URL url) {
        return BlockingTask
            // This call ensures the common fork/join thread pool
            // is expanded to handle the blocking image download.
            .callInManagedBlock(()
                                -> downloadImage(url))

            // Store the image on the local device.
            .store();
    }

    /**
     * Factory method that retrieves the image associated with the @a
     * url and creates an Image to encapsulate it.
     */
    private Image downloadImage(URL url) {
        return new Image(url,
                         NetUtils.downloadContent(url));
    }

    /**
     * Clears the filter directories.
     */
    private void deleteDownloadedImages() {
        int deletedFiles =
            deleteSubFolders(Options.instance().getDirectoryPath());

        System.out.println(TAG
                           + ": "
                           + deletedFiles
                           + " previously downloaded file(s) deleted");
    }

    /**
     * Recursively delete files in a specified directory.
     */
    private int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File files[] = currentFolder.listFiles();

        if (files == null) 
            return 0;

        // Java doesn't delete a directory with child files, so we
        // need to write code that handles this recursively.
        for (File f : files) {          
            if (f.isDirectory()) 
                deletedFiles += deleteSubFolders(f.toString());
            f.delete();
            deletedFiles++;
        }

        // Don't delete the current folder.
        // currentFolder.delete();
        return deletedFiles;
    }
}
