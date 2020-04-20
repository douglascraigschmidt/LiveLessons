package platspec;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Java platform.
 */
public class ImageBase {
    /**
     * The Bitmap our Image stores.
     */
    public BufferedImage mImage;

    /**
     * Returns the @a Bitmap stored by this Image.
     */
    public BufferedImage getImage() {
        return mImage;
    }

    /**
     * Decodes a byte[] into an @a Image that can be used in the rest
     * of the application.
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
     * Set the Image from the @a image object.
     */
    public void setImage(Object image) {
        mImage = (BufferedImage) image;
    }
}
