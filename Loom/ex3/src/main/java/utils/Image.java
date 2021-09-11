package utils;

import platspec.ImageBase;
import platspec.PlatSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Stores platform-independent meta-data about an Image and also
 * provides methods for common image- and file-related tasks, such as
 * decoding raw byte arrays into an Image and setting/getting image
 * and file names.
 */
public class Image 
       extends ImageBase {
    /**
     * The source URL from which the result was downloaded.
     */
    private URL mSourceUrl;

    /**
     * Construct an Image from a byte array of @a imageData
     * downloaded from a URL @a source.
     */
    public Image(URL sourceURL,
                 byte[] imageData) {
        // Set the URL.
        mSourceUrl = sourceURL;
        
        // Decode the imageData into a Bitmap.
        setImage(imageData);
    }

    /**
     * Returns the file name from the URL this Image was
     * constructed from.
     */
    private String getFileName() {
        return FileAndNetUtils.getFileNameForUrl(mSourceUrl);
    }

    /**
     * Store the image on the local file system.
     *
     * @return The stored image file.
     */
    public File store() {
        // Get a reference to the file in which the image will be
        // stored.
        File imageFile = new File(new File(Options.instance().getDirectoryPath()),
                                  getFileName());
        
        // Store the image using try-with-resources
        
        try (FileOutputStream outputFile =
             new FileOutputStream(imageFile)) {
            // Write the image to the output file.
            PlatSpec.writeImageFile(outputFile, this);
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
