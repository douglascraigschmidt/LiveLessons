package utils;

import platspec.ImageBase;
import platspec.PlatSpec;
import transforms.Transform;
import transforms.Transforms;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import static utils.ExceptionUtils.rethrowSupplier;

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
    private final URL mSourceUrl;

    /**
     * Construct an Image from a byte array of {@code imageData}
     * downloaded from a URL {@code sourceUrl}.
     */
    public Image(URL sourceURL,
                 byte[] imageData) {
        super(rethrowSupplier(() -> ImageIO
                              .read(new ByteArrayInputStream(imageData)))
                              .get());

        // Set the URL.
        mSourceUrl = sourceURL;
    }

    /**
     * Constructor initialized by the {@link BufferedImage}
     */
    private Image(BufferedImage bufferedImage,
                  URL sourceURL) {
        super(bufferedImage);
        mSourceUrl = sourceURL;
    }

    /**
     * Returns the file name from the URL this Image was
     * constructed from.
     */
    private String getFileName() {
        return FileAndNetUtils.getFileNameForUrl(mSourceUrl);
    }

    /**
     *
     */
    public Image applyTransform(Transform.Type type) {
        BufferedImage originalImage = mImage;
        BufferedImage filteredImage =
            new BufferedImage
            (originalImage.getColorModel(),
             originalImage.copyData(null),
             originalImage.getColorModel().isAlphaPremultiplied(),
             null);

        int[] pixels = filteredImage
            .getRGB(0, 0, filteredImage.getWidth(), filteredImage.getHeight(),
                    null, 0, filteredImage.getWidth());

        int[] lastProgress = new int[1];

        switch (type) {
        case GRAY_SCALE_TRANSFORM:
            Transforms.grayScale(pixels, filteredImage.getColorModel().hasAlpha());
            break;
        case TINT_TRANSFORM:
            Transforms.tint(pixels, filteredImage.getColorModel().hasAlpha(),
                            0.0f, 0.0f, 0.9f);
            break;
        case SEPIA_TRANSFORM:
            Transforms.sepia(pixels, filteredImage.getColorModel().hasAlpha());
            break;
        default:
            return this;
        }

        filteredImage
            .setRGB(0, 0, filteredImage.getWidth(), filteredImage.getHeight(),
                    pixels, 0, filteredImage.getWidth());

        return new Image(filteredImage,
                         mSourceUrl);
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
