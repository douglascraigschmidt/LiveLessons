package utils;

import common.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.io.File;

/**
 * Provides some general utility helper methods for file and network
 * operations.
 */
public final class FileAndNetUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = FileAndNetUtils.class.getName();

    /**
     * A utility class should always define a private constructor.
     */
    private FileAndNetUtils() {
    }

    /**
     * Download the contents found at the given URL and return them as
     * a raw byte array.
     */
    public static byte[] downloadContent(URL url) {
        // The size of the image downloading buffer.
        final int BUFFER_SIZE = 4096;

        // Creates a new ByteArrayOutputStream to write the downloaded
        // contents to a byte array, which is a generic form of the
        // image.
        ByteArrayOutputStream ostream =
            new ByteArrayOutputStream();
        
        // This is the buffer in which the input data will be stored
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytes;
        
        // Creates an InputStream from the inputUrl from which to read
    	// the image data.
        try (InputStream istream = url.openStream()) {
            // While there is unread data from the inputStream,
            // continue writing data to the byte array.
            while ((bytes = istream.read(readBuffer)) > 0) 
                ostream.write(readBuffer, 0, bytes);

            return ostream.toByteArray();
        } catch (IOException e) {
            // "Try-with-resources" will clean up the istream
            // automatically.
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Transform a {@link URL} to an {@link Image} by downloading the
     * contents of the {@code url}.
     *
     * @param url The {@link URL} of the image to download
     * @return An {@link Image} containing the image contents
     */
    public static Image downloadImage(URL url) {
        return 
            // Perform a blocking image download.
            new Image(url, BlockingTask
                      // This call ensures the common fork/join thread
                      // pool is expanded to handle the blocking image
                      // download.
                      .callInManagedBlock(() -> 
                                          downloadContent(url)));
    }
    
    /**
     * This method stores the {@link Image} to the local file system.
     *
     * @return A {@link File} containing the stored {@link Image}
     */
    public static File storeImage(Image image) {
        return image.store();
    }

    /**
     * @ return A filename form of the {@code url}.
     */
    public static String getFileNameForUrl(URL url,
                                           String transformName) {
        // Just use the host and "filename".
        String uriName = url.getHost() + url.getFile();

        // Replace useless characters with UNDERSCORE.
        String fileName = uriName.replaceAll("[./:]", "_");

        // Replace last underscore with a dot
        fileName = fileName.substring(0, fileName.lastIndexOf('_'))
            + "_"
            + transformName
            + "."
            + fileName.substring(fileName.lastIndexOf('_') + 1);
        return fileName;
    }


    /**
     * Clears the filter directories.
     */
    public static void deleteDownloadedImages(String directoryPath) {
        int deletedFiles =
            deleteSubFolders(directoryPath);

        if (Options.instance().diagnosticsEnabled())
            System.out.println(TAG
                               + ": "
                               + deletedFiles
                               + " previously downloaded file(s) deleted");
    }

    /**
     * Recursively delete files in a specified directory.
     */
    public static int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File[] files = currentFolder.listFiles();

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
