package livelessons.imagestreamgang.filters;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import livelessons.imagestreamgang.ImageEntity;
import livelessons.imagestreamgang.Options;

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
public class OutputFilterDecorator 
       extends FilterDecorator {
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
    @Override
    protected ImageEntity decorate(ImageEntity imageEntity) {
        // Store the filtered image as its filename (which is derived
        // from its URL), within the appropriate filter directory to
        // organize the filtered results and write the image to 
    	// the file in the appropriate directory.

    	// Ensure that the path exists.
        File externalFile = new File(Options.instance().getDirectoryPath(),
                                     this.getName());
        externalFile.mkdirs();
        
        // Get a reference to the file in which the image will be stored
        File imageFile = new File(externalFile, imageEntity.getFileName());
        
        // Store the image using try-with-resources
        try (FileOutputStream outputFile =
             new FileOutputStream(imageFile)) {
                Bitmap image = imageEntity.getImage().getBitmap();
                if (image == null)
                    Log.e(TAG, "null Bitmap");
                else
                    image.compress(Bitmap.CompressFormat.PNG,
                                   100,
                                   outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return imageEntity;
    }
}
