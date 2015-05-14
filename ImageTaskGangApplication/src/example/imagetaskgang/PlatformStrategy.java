package example.imagetaskgang;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


import android.annotation.SuppressLint;

/** 
 * @class PlatformStrategy
 *
 * @brief Provides methods that define a platform-independent
 *        mechanism for getting URLs to download, as well as creating,
 *        processing, and storing URLs.  This class is a singleton
 *        that also plays the role of the "Strategy" in the Strategy
 *        pattern and the Product in the Factory Method pattern.  Both
 *        the PlatformStrategyConsole and PlatformStrategyAndroid
 *        subclasses extend this class.
 */
public abstract class PlatformStrategy {
    /** 
     * The singleton @a PlatformStrategy instance. 
     */
    private static PlatformStrategy sUniqueInstance = null;

    /** 
     * Method to return the one and only singleton instance. 
     */
    public static PlatformStrategy instance() {
        return sUniqueInstance;
    }

    /** 
     * Method that sets a new PlatformStrategy singleton and returns
     * the one and only singleton instance.
     */
    public static PlatformStrategy instance(PlatformStrategy platform) {
        return sUniqueInstance = platform;
    }
    
    /**
     * Return one or more input URL Lists.
     */
    public abstract List<List<URL>> getUrlLists(InputSource source);

    /**
     * Return an Iterator over one or more input URL Lists.
     */
    public Iterator<List<URL>> getUrlIterator(InputSource source) {
    	List<List<URL>> urlLists =
    			PlatformStrategy.instance().getUrlLists(source);
    	return (urlLists != null && urlLists.size() > 0) ? 
    			urlLists.iterator() : null;
    }
    
    /**
     * Return the path for the directory where images are stored.
     */
    public abstract String getDirectoryPath();
    
    /**
     * A method to make the directories and get a file reference
     * on the current platform, returning the success or failure
     */
    @SuppressLint("NewApi")
    public boolean storeExternalImage(String pathName,
    			String fileName, Image image) {
    	// Ensure that the path exists
		File externalFile = 
	            new File(PlatformStrategy.instance().getDirectoryPath(),
	                     pathName);
        externalFile.mkdirs();
        
        // Get a reference to the file in which the image will be stored
        File imageFile = new File(externalFile, fileName);
        
        // Store the image using try-with-resources
        try (FileOutputStream outputFile =
                new FileOutputStream(imageFile)) {
           PlatformStrategy.instance().storeImage(image, outputFile);
        }
        catch (Exception e) {
	       e.printStackTrace();
	       return false;
        }
        
        return true;
	}

    /**
     * Factory method that creates an @a Image from a byte array.
     */
    public abstract Image makeImage(byte[] imageData);

    /**
     * Apply a grayscale filter to the @a imageEntity and return it.
     */
    public abstract ImageEntity grayScaleFilter(ImageEntity imageEntity);

    /**
     * Store the @a image in the given @outputFile.
     */
    public abstract void storeImage(Image image,
                                    FileOutputStream outputFile);

    /**
     * Error log formats the message and displays it for debugging
     * purposes.
     */
    public abstract void errorLog(String javaFile,
                                  String errorMessage);
    
    /**
     * Make the constructor protected to ensure singleton access.
     */
    protected PlatformStrategy() {}

    /**
     * An enumeration of each different input source.
     */
    public static enum InputSource {
    	DEFAULT, // The default input source that's shared between
                 // platforms.
        USER,    // Input from a user-defined source, such as the
                 // Android UI or console command-line.
        FILE,    // Input from a delimited file.
        NETWORK, // Input from a network call
        ERROR    // Returned if source is unrecognized.
    }
    
    /**
     * Takes a string input and returns the corresponding InputSource.
     */
    public InputSource getInputSource(String inputSource) {
        if (inputSource.equalsIgnoreCase("DEFAULT")) 
            return InputSource.DEFAULT;
        else if (inputSource.equalsIgnoreCase("USER")) 
            return InputSource.USER;
        else if (inputSource.equalsIgnoreCase("FILE")) 
            return InputSource.FILE;
        else if (inputSource.equalsIgnoreCase("NETWORK"))
        	return InputSource.NETWORK;
        else 
            return InputSource.ERROR;
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    protected List<List<URL>> getDefaultUrlList() throws MalformedURLException {
    	final List<List<URL>> variableNumberOfInputURLs = 
                new ArrayList<List<URL>>();

        final URL[] urls1 = {        
            new URL("http://www.dre.vanderbilt.edu/~schmidt/ka.png"),
            new URL("http://www.dre.vanderbilt.edu/~schmidt/uci.png"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/douglass.jpg")
        };
        final URL[] urls2 = {
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/lil-doug.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg")
        };
        variableNumberOfInputURLs.add(Arrays.asList(urls1));
        variableNumberOfInputURLs.add(Arrays.asList(urls2));

    	return variableNumberOfInputURLs;
    }
}
