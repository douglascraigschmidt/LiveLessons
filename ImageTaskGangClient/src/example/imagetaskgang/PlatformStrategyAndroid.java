package example.imagetaskgang;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.AutoCompleteTextView;

/**
 * @class PlatformStrategyAndroid
 * 
 * @brief Provides methods that define a platform-independent
 *        mechanism for getting URLs to download, as well as creating,
 *        processing, and storing URLs.  It plays the role of the
 *        "Concrete Strategy" in the Strategy pattern.
 */
public class PlatformStrategyAndroid extends PlatformStrategy {
    /** 
     * Define a WeakReference to avoid memory leaks. 
     */
    private final WeakReference<MainActivity> mOuterClass;

    /**
     * The path to the external storage directory in Android.
     */
    final static String EXTERNAL_PATH = 
        Environment.getExternalStorageDirectory().toString();
	
    /**
     * Constructor initializes the data member.
     */
    public PlatformStrategyAndroid(final Object output) {
        /** The current activity window (succinct or verbose). */
        mOuterClass = new WeakReference<MainActivity>
            ((MainActivity) output);
    }
    
    /**
     * Gets the list of lists of URLs from which
     * we want to download images.
     */
    public List<List<URL>> getUrlLists(InputSource source) {
    	List<List<URL>> variableNumberOfInputURLs = 
                new ArrayList<List<URL>>();
        	
    	try {
            switch (source) {
            // If the user selects the defaults source, return
            // the default list of URL lists. Works on both console
            // and Android platforms
            case DEFAULT:
                variableNumberOfInputURLs = super.getDefaultUrlList();
                break;
	            
            // Take input from the Android UI
            case USER:
            	
            	// Check if the Activity still exists
                if (mOuterClass.get() != null) {
                	
                	// Iterate over the children of the LinearLayout
                	// that holds the list of URL lists
                    int numChildViews = 
                        mOuterClass.get().mListUrlGroups.getChildCount();
                    for (int i = 0; i < numChildViews; ++i) {
                        AutoCompleteTextView child = 
                            (AutoCompleteTextView) 
                            mOuterClass.get().mListUrlGroups.getChildAt(i);
                        
                        // Create a new URL list and add each URL
                        // separated by commas to the list
                        List<URL> urls = new ArrayList<URL>();
                        StringTokenizer tokenizer = 
                            new StringTokenizer(child.getText().toString(), ", ");
                        while (tokenizer.hasMoreTokens()) {
                            urls.add(new URL(tokenizer.nextToken().trim()));
                        }
                        
                        // Add the list of URLs to the main list
                        variableNumberOfInputURLs.add(urls);
                    }
                }
                break;
    			
            default:
                if (mOuterClass.get() != null) 
                    mOuterClass.get().showToast("Invalid Source");
                return null;
            }
    	} catch (MalformedURLException e) {
            if (mOuterClass.get() != null) 
                mOuterClass.get().showToast("Invalid URL");
            return null;
    	}
    	
    	return variableNumberOfInputURLs;
    }

    /**
     * Return the path for the directory where images are stored.
     */
    public String getDirectoryPath() {
        return EXTERNAL_PATH;
    }

    /**
     * Factory method that creates an @a Image from a byte array.
     */
    public Image makeImage(byte[] imageData){
        return new BitmapImage(imageData);
    }

    /**
     * Apply a grayscale filter to the @a imageEntity and return it.
     */
    public ImageEntity grayScaleFilter(ImageEntity imageEntity) {
        Image imageAdapter =
            ((ImageEntity) imageEntity).getImage();
        Bitmap originalImage =
            ((BitmapImage) imageAdapter).mBitmap;

        Bitmap grayScaleImage =
            originalImage.copy(originalImage.getConfig(), true);

        boolean hasTransparent = grayScaleImage.hasAlpha();
        int width = grayScaleImage.getWidth();
        int height = grayScaleImage.getHeight();

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
            	
            	// Check if the pixel is transparent in the original
            	// by checking if the alpha is 0
                if (hasTransparent 
                    && ((grayScaleImage.getPixel(j, i) & 0xff000000) >> 24) == 0) {
                    continue;
                }
                
                // Convert the pixel to grayscale.
                int pixel = grayScaleImage.getPixel(j, i);
                int grayScale = 
                    (int) (Color.red(pixel) * .299 
                           + Color.green(pixel) * .587
                           + Color.blue(pixel) * .114);
                grayScaleImage.setPixel(j, i, 
                                     Color.rgb(grayScale, grayScale, grayScale)
                                     );
            }
        }

        // Encapsulate the Android-specific Bitmap object within a
        // platform-independent BitMapImage object.
        BitmapImage bitmapImage =
            new BitmapImage(grayScaleImage);

        // Return an ImageEntity containing the filtered image.
        return new ImageEntity(imageEntity.getSourceURL(),
                               bitmapImage);
    }
    
    /**
     * Store the @a image in the given @outputFile.
     */
    public void storeImage(Image imageAdapter,
                           FileOutputStream outputFile) {
    	((BitmapImage) imageAdapter).mBitmap.compress(Bitmap.CompressFormat.PNG,
                                                      100,
                                                      outputFile);
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile,
                         String errorMessage) {
        Log.e(javaFile, errorMessage);
    }
}
