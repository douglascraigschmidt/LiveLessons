package platspec;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A utility class containing static methods whose implementations are
 * specific to a particular platform (e.g., the Java platform vs. the
 * Android platform).  This implementation is specific for the Java
 * platform.
 */
public final class PlatSpec {
    /**
     * Logging tag.
     */
    private static final String TAG = PlatSpec.class.getName();

    /**
     * A utility class should always define a private constructor.
     */
    private PlatSpec() {
    }
    
    /**
     * Creates an input stream for the passed URL. This method will
     * support both normal URLs and any URL located in the application
     * resources.
     *
     * @param url     Any URL including a resource URL.
     * @return An input stream.
     */
    public static InputStream getInputStream(URL url)
            throws IOException {
       // Normal URL.
       return url.openStream();
    }

    /**
     * Write the @a image to the @a outputStream.
     */
    public static void writeImageFile(FileOutputStream outputStream,
                                      ImageBase image) throws IOException {
        BufferedImage bufferedImage = image.getImage();

        if (bufferedImage == null)
            System.out.println("null image");
        else 
            ImageIO.write(bufferedImage,
                          "png",
                          outputStream);
    }
}
