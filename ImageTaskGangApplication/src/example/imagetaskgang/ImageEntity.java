package example.imagetaskgang;

import java.net.URL;

/**
 * @class ImageEntity
 *
 * @brief Extends InputEntity and defines the logic for decoding raw
 *        byte arrays into an Image that can be used in the rest of
 *        the application. Also handles common file-related tasks,
 *        such as retrieval of the native format and the original file
 *        name of the Image.
 */
public class ImageEntity extends InputEntity {
    /**
     * The Image our ImageEntity stores.
     */
    private Image mImage;

    /**
     * Construct an ImageEntity from a byte array of @a imageData
     * downloaded from a URL @a source.
     */
    public ImageEntity(URL source,
                       byte[] imageData) {
        // Call up to the superclass constructor.
        super(source);
        
        // Decode the imageData into a Bitmap.
        setImage(imageData);
    }

    /**
     * Construct a new ImageEntity from an @a Image.
     */
    public ImageEntity(URL source,
                       Image image) {
        // Call up to the superclass constructor.
        super(source);

        // Store the image in the data member.
        mImage = image;
    }

    /**
     * Returns the @a Image stored by this ImageEntity.
     */
    public Image getImage() {
        return mImage;
    }

    /**
     * Decodes a byte[] into an @a Image that can be used in the rest
     * of the application.
     */
    public void setImage(byte[] imageData) {
        mImage = PlatformStrategy.instance().makeImage(imageData);
    }

    /**
     * This method is not strictly needed by our ImageEntity, but is a
     * place holder for future enhancements that must manipulate byte
     * arrays in different ways.
     */
    @Override
    public ImageEntity decodeBytesToResult(byte[] data) {
        setImage(data);
        return this;
    }

    /**
     * Returns the file name from the URL this ImageEntity was
     * constructed from.
     */
    public String getFileName() {
        return mSourceUrl.getFile().substring
            (mSourceUrl.getFile().lastIndexOf('/'));
    }

    /**
     * Returns the format of the image from the URL in string form.
     */
    public String getFormatName() {
        String format =
            mSourceUrl.getFile().substring
            (mSourceUrl.getFile().lastIndexOf('.') + 1);
        format = format.equalsIgnoreCase("jpeg") ? "jpg" : format;
        return format;
    }
    
}
