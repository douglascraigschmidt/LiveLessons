package example.imagetaskgang;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


// @@ import javax.imageio.ImageIO;

/**
 * @class PlatformStrategyConsole
 *
 * @brief Provides methods that define a platform-independent
 *        mechanism for getting URLs to download, as well as creating,
 *        processing, and storing URLs.  It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern.
 */
@SuppressLint("NewApi")
public class PlatformStrategyConsole extends PlatformStrategy {
    /**
     * Contains information for printing output to the console window.
     */
    private final PrintStream mOutput;

    /** 
     * Constructor initializes the data member.
     */
    public PlatformStrategyConsole(Object output) {
        mOutput = (PrintStream) output;
    }
    
    /**
     * Overrides the getURLIterator method to return the
     * Console-specific input sources.
     */
	public List<List<URL>> getUrlLists(InputSource source) {
		List<List<URL>> variableNumberOfInputURLs = 
	            new ArrayList<List<URL>>();
	    	
    	try {
            switch (source) {
            // If the user selects the defaults source, return the
            // default list of URL lists. Works on both console and
            // Android platforms.
            case DEFAULT:
                variableNumberOfInputURLs =
                    super.getDefaultUrlList();
                break;
	           
            // Read a list of URL lists from a delimited file.
            case FILE:
                try (BufferedReader urlReader = 
                     new BufferedReader(new FileReader
                    		 (Options.instance().getURLFilePathname()))) {
                        List<URL> currentUrls = new ArrayList<URL>();
                    
                        // Iterator over each line in the file
                        for (String url; 
                             (url = urlReader.readLine()) != null;) {
                    	
                            // If the line is the dedicated delimiter,
                            // add the current list to the main list,
                            // and start a new list.
                            if (url.equalsIgnoreCase
                                (Options.instance().getSeparator())) {
                                variableNumberOfInputURLs.add(currentUrls);
                                currentUrls = new ArrayList<URL>();
                            }
                            // Otherwise, add the URL to current list.
                            else
                                currentUrls.add(new URL(url));
                        }
                    
                        // Add the final list to the main list.
                        variableNumberOfInputURLs.add(currentUrls);
			    	
                    } catch (FileNotFoundException e) {
                    mOutput.println("URL file not found");
                    return null;
                } catch (IOException e) {
                    mOutput.println("Error reading file");
                    return null;
                } 
                break;
    			
            default:
                mOutput.println("Invalid Source");
                return null;
            }
    	} catch (MalformedURLException e) {
            mOutput.println("Invalid URL");
            return null;
    	}
    	
    	return variableNumberOfInputURLs;
	}
	
    /**
     * Return the path for the directory where images are stored.
     */
    public String getDirectoryPath() {
        return new File("DownloadImages").getAbsolutePath();
    }

    /**
     * Factory method that creates an @a Image from a byte array.
     */
    public Image makeImage(byte[] imageData){
        // @@ return new BufferedImage(imageData);
    	return null;
    }
     
    /**
     * Apply a grayscale filter to the @a imageEntity and return it.
     */
    public ImageEntity grayScaleFilter(ImageEntity imageEntity) {
        /* @@
    	Image imageAdapter =
            ((ImageEntity) imageEntity).getImage();
        java.awt.image.BufferedImage originalImage = 
            ((BufferedImage) imageAdapter).mBufferedImage;
        java.awt.image.BufferedImage grayScaleImg =
            new java.awt.image.BufferedImage
            (originalImage.getColorModel(),
             originalImage.copyData(null),
             originalImage.getColorModel().isAlphaPremultiplied(),
             null);
    
        boolean hasTransparent =
            grayScaleImg.getColorModel().hasAlpha();
        int width = grayScaleImg.getWidth();
        int height = grayScaleImg.getHeight();
    
        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
            	
            	// Check if the pixel is transparent in the original.
                if (hasTransparent 
                    && (grayScaleImg.getRGB(j,
                                            i) >> 24) == 0x00) 
                    continue;
                
                // Convert the pixel to grayscale.
                Color c = new Color(grayScaleImg.getRGB(j,
                                                        i));
                int grayConversion =
                    (int) (c.getRed() * 0.299)
                    + (int) (c.getGreen() * 0.587)
                    + (int) (c.getBlue() * 0.114);
                Color grayScale = new Color(grayConversion,
                                            grayConversion,
                                            grayConversion);
                grayScaleImg.setRGB(j, i, grayScale.getRGB());
            }
        }
   	
    	BufferedImage grayScaleImage = 
            new BufferedImage(grayScaleImg);

        return new ImageEntity(imageEntity.getSourceURL(),
                               grayScaleImage);
        */
        return null;
    }
    
    /**
     * Store the @a image in the given @outputFile.
     */
    public void storeImage(Image imageAdapter,
                           FileOutputStream outputFile) {
        /* @@
    	// Write the image to the appropriate directory.
        try {
            ImageIO.write(((BufferedImage) imageAdapter).mBufferedImage,
                          "png",
                          outputFile);
        } catch (IOException e) {
            mOutput.println("ImageIO write failure");
            e.printStackTrace();
        }
        */
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile, String errorMessage) {
        mOutput.println(javaFile 
                        + " " 
                        + errorMessage);
    }
    
}
