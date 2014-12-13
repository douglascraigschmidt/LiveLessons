package example.imagetaskgang;

import android.annotation.SuppressLint;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @class OutputFilterDecorator
 *
 * @brief A Decorator whose inherited applyFilter() template method
 *        calls the filter() method on the Filter object passed to its
 *        constructor and whose decorate() hook method then writes the
 *        results of the filtered image to an output file.  Plays the
 *        role of the "Concrete Decorator" in the Decorator pattern
 *        and the role of the "Concrete Class" in the Template Method
 *        pattern.
 */
public class OutputFilterDecorator extends FilterDecorator {
    /**
     * Constructor passes the @a filter parameter up to the superclass
     * constructor, which stores it in a data member for subsequent
     * use in applyFilter(), which is both a hook method and a
     * template method.
     */
    public OutputFilterDecorator(Filter filter) {
    	super(filter);
    }

    /**
     * This hook method is called with the @a imageEntity parameter
     * after it has been filtered with mFilter in the inherited
     * applyFilter() method.  decorate() stores the filtered
     * ImageEntity in a file.
     */
    @SuppressLint("NewApi")
    @Override
    protected ImageEntity decorate(ImageEntity imageEntity) {
        // Make a directory for the filtered image if it does not
        // exist already.
        File externalFile = 
            new File(PlatformStrategy.instance().getDirectoryPath(),
                     this.getName());
        externalFile.mkdirs();
        
        // Store the filtered image as its filename (which is derived
        // from its URL), within the appropriate filter directory to
        // organize the filtered results.
        File newImage = 
            new File(externalFile, 
                     imageEntity.getFileName());
        
        // Write the image to the file in the appropriate directory.
        // The close() method of the outputFile is automatically
        // called via the Java "try-with-resources" feature.
        try (FileOutputStream outputFile =
             new FileOutputStream(newImage)) {
                PlatformStrategy.instance().storeImage
                    (imageEntity.getImage(),
                     outputFile);
            } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return imageEntity;
    }
}
