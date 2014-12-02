package example.imagetaskgang;

import android.annotation.SuppressLint;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @class OutputFilterDecorator
 *
 * @brief A Decorator that applies the filter passed to its
 *        constructor and then writes the results to an output file.
 *        Plays the role of the "Concrete Decorator" in the Decorator
 *        pattern.
 */
public class OutputFilterDecorator extends FilterDecorator {
    /**
     * Constructs the filter decorator with the @a filter to apply.
     */
    public OutputFilterDecorator(Filter filter) {
    	super(filter);
    }

    /**
     * The hook method that is called on the InputEntity once it has
     * been filtered with mFilter.  This method stores the filtered
     * InputEntity in a file by delegating the storing to the
     * platform- specific implementation of storeImage(...).
     */
    @SuppressLint("NewApi")
    @Override
    protected InputEntity decorate(InputEntity inputEntity) {
        if (android.os.Environment.getExternalStorageState().equals
            (android.os.Environment.MEDIA_MOUNTED)) {
            // Call the applyFilter() hook method.
            ImageEntity result = (ImageEntity) inputEntity;
		
            // Make a directory for the filter if it does not already
            // exist.
            File externalFile = 
                new File(PlatformStrategy.instance().getDirectoryPath(),
                         this.getName());
            externalFile.mkdirs();
        
            // We will store the filtered image as its original filename,
            // within the appropriate filter directory to organize the
            // filtered results.
            File newImage = 
                new File(externalFile, 
                         result.getFileName());
        
            // Write the compressed image to the appropriate directory.
            try (FileOutputStream outputFile = new FileOutputStream(newImage)) {
                    PlatformStrategy.instance().storeImage(result.getImage(),
                                                           outputFile);
                } catch (Exception e) {
                // Try-with-resources will clean up resources.
                e.printStackTrace();
                return null;
            }

            return result;
        } else {
            PlatformStrategy.instance().errorLog
                ("OutoutFileDecorator",
                 "sdcard isn't mounted");
            return null;
        }
    }
}
