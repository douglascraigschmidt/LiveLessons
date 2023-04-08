package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Stores platform-independent meta-data about an Image and also
 * provides methods for common image- and file-related tasks, such as
 * decoding raw byte arrays into an Image and setting/getting image
 * and file names.
 */
public class Image {
    /**
     * The Bitmap our Image stores.
     */
    public BufferedImage mImage;

    /**
     * Returns the {@link BufferedImage} stored by this Image.
     */
    public BufferedImage getImage() {
        return mImage;
    }

    /**
     * The source URL from which the result was downloaded.
     */
    private final URL mSourceUrl;

    /**
     * Decodes a byte[] into an {@link Image} that can be used in the
     * rest of the application.
     */
    public void setImage(byte[] imageData) {
        try {
            mImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set the Image from the {@link Image} object.
     */
    public void setImage(Object image) {
        mImage = (BufferedImage) image;
    }

    /**
     * Construct an Image from a byte array of {@code imageData}
     * downloaded from the {@link URL}.
     */
    public Image(URL sourceURL,
                 byte[] imageData) {
        // Set the URL.
        mSourceUrl = sourceURL;
        
        // Decode the imageData into a Bitmap.
        setImage(imageData);
    }

    /**
     * Returns the file name from the URL this {@link Image} was
     * constructed from.
     */
    private String getFileName() {
        return NetUtils.getFileNameForUrl(mSourceUrl);
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
            writeImageFile(outputFile, this);
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write the {@link Image} to the {@link FileOutputStream}.
     */
    public static void writeImageFile(FileOutputStream outputStream,
                                      Image image) throws IOException {
        BufferedImage bufferedImage = image.getImage();

        if (bufferedImage == null)
            System.out.println("null image");
        else 
            ImageIO.write(bufferedImage,
                          "png",
                          outputStream);
    }
}
