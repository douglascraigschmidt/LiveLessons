package livelessons.imagestreamgang.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static livelessons.imagestreamgang.TheApp.getApp;

/**
 * Provides some general utility helper methods for network operations.
 */
public final class NetUtils {
    // To refer to bar.png under your package's res/drawable/ directory, use
    // "file:///android_res/drawable/bar.png". Use "drawable" to refer to
    // "drawable-hdpi" directory as well.
    static final String RESOURCE_BASE = "file:///android_res/";

    /**
     * Logging tag.
     */
    private static final String TAG = "NetUtils";

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
     * Creates an input stream for the passed URL. This method will
     * support both normal URLs and any URL located in the application
     * resources.
     *
     * @param url     Any URL including a resource URL.
     * @return An input stream.
     * @throws IOException
     */
    private static InputStream getInputStream(URL url)
            throws IOException {
        if (isResourceUrl(url.toString())) {
            Log.d(TAG, "Loading image from app resources");

            // Both URL and Uri classes will not accept a proper
            // android resource scheme but will accept the prefix
            // "file:///android_res/". To get an apk resource input
            // stream, simply replace this prefix so that the
            // resulting url can be passed to the application's
            // content resolver.
            String resUrl = 
                url.toString().replace(RESOURCE_BASE,
                                       ContentResolver.SCHEME_ANDROID_RESOURCE
                                       + "://");
            return getApp().getContentResolver().openInputStream(Uri.parse(resUrl));
        } else {
            // Normal URL.
            return url.openStream();
        }
    }

    /**
     * @return True iff the url is a resource file.
     */
    private static boolean isResourceUrl(String url) {
        return (null != url) 
            && url.startsWith(RESOURCE_BASE);
    }

    /**
     * Returns a filename form of the @a url.
     */
    public static String getFileNameForUrl(URL url) {
        // Just use the host and "filename".
        String uriName = url.getHost() + url.getFile();

        // Replace useless chareacters with UNDERSCORE
        String fileName = uriName.replace(".", "_").replace("/", "_");

        // Replace last underscore with a dot
        fileName = fileName.substring(0, fileName.lastIndexOf('_'))
            + "."
            + fileName.substring(fileName.lastIndexOf('_') + 1,
                                   fileName.length());
        return fileName;
    }
}
