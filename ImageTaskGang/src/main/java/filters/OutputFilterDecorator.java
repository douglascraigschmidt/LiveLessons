package filters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import utils.Image;
import utils.Options;

/**
 * A Decorator whose inherited {@code applyFilter()} template method
 * calls the {@code filter()} method on the {@link Filter} passed to
 * its constructor and whose {@code decorate()} hook method then
 * writes the results of the filtered {@link Image} to an output file.
 *
 * {@link OutputFilterDecorator} plays the role of the "Concrete
 * Decorator" in the Decorator pattern and the role of the "Concrete
 * Class" in the Template Method pattern.
 */
public class OutputFilterDecorator 
       extends FilterDecorator {
    /**
     * Constructor passes the {@link Filter} parameter up to the
     * superclass constructor, which stores it in a data member for
     * subsequent use in {@code applyFilter()}, which is both a hook
     * method and a template method.
     */
    public OutputFilterDecorator(Filter filter) {
    	super(filter);
    }

    /**
     * This hook method is called with the {@link Image} parameter
     * after it has been filtered via the inherited {@code
     * applyFilter()} method.  It stores the filtered {@link Image} in
     * a file.
     *
     * @return The filtered and stored {@link Image} 
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected Image decorate(Image image) {
        // Store the filtered image as its filename (which is derived
        // from its URL), within the appropriate filter directory to
        // organize the filtered results and write the image to the
        // file in the appropriate directory.

    	// Ensure that the path exists.
        File externalFile = new File(Options.instance().getDirectoryPath(),
                                     this.getName());
        externalFile.mkdirs();
        
        // Get a reference to the file in which the image will be
        // stored.
        File imageFile = new File(externalFile,
                                  image.getFileName());
        
        // Store the image using try-with-resources.
        try (FileOutputStream outputFile =
             new FileOutputStream(imageFile)) {
            BufferedImage bi = image.getImage();

            // Write the image in PNG format.
            ImageIO.write(bi,
                          "png",
                          outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return image;
    }
}
