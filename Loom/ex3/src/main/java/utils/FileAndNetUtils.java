package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.io.File;

import static platspec.PlatSpec.getInputStream;

/**
 * Provides some general utility helper methods for file and network
 * operations.
 */
public final class FileAndNetUtils {
    // To refer to bar.png under your package's res/drawable/ directory, use
    // "file:///android_res/drawable/bar.png". Use "drawable" to refer to
    // "drawable-hdpi" directory as well.
    static final String RESOURCE_BASE = "file:///android_res/";

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
        try (InputStream istream = getInputStream(url)) {
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
     * @return True iff the url is a resource file.
     */
    public static boolean isResourceUrl(String url) {
        return (null != url) 
            && url.startsWith(RESOURCE_BASE);
    }

    /**
     * Returns a filename form of the @a url.
     */
    public static String getFileNameForUrl(URL url) {
        // Just use the host and "filename".
        String uriName = url.getHost() + url.getFile();

        // Replace useless characters with UNDERSCORE.
        String fileName = uriName.replaceAll("[./:]", "_");

        // Replace last underscore with a dot
        fileName = fileName.substring(0, fileName.lastIndexOf('_'))
            + "."
            + fileName.substring(fileName.lastIndexOf('_') + 1,
                                 fileName.length());
        return fileName;
    }


    /**
     * Clears the filter directories.
     */
    public static void deleteDownloadedImages() {
        int deletedFiles =
            deleteSubFolders(Options.instance().getDirectoryPath());

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
