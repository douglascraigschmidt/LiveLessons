package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static platspec.PlatSpec.getInputStream;

/**
 * A Java utility class that provides helper methods for network
 * operations.
 */
public final class NetUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = NetUtils.class.getName();

    /** 
     * To refer to bar.png under your package's res/drawable/
     * directory, use "file:///android_res/drawable/bar.png". Use
     * "drawable" to refer to "drawable-hdpi" directory as well.
     */
    static final String RESOURCE_BASE = "file:///android_res/";

    /**
     * A utility class should always define a private constructor.
     */
    private NetUtils() {
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
}
