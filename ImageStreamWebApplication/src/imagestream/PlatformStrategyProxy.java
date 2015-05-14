package imagestream;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

/**
 * @class PlatformStrategyProxy
 * 
 * @brief This class is a Proxy that extends PlatformStrategyConsole
 *        to get the URLs from the appropriate Servlet directory.
 */
public class PlatformStrategyProxy extends PlatformStrategyConsole {
    /**
     * Directory used by the Servlet.
     */
    private String mServletTempDir;
    
    /**
     * List of URLs.
     */
    private List<List<URL>> mInputURLs;
	
    /**
     * Constructor.
     */
    public PlatformStrategyProxy(Object output,
                                 ServletContext servletContext, 
                                 List<List<URL>> requestUrls) {
        super(output);
        mServletTempDir =
            servletContext.getAttribute(ServletContext.TEMPDIR).toString();
        mOutput.println("Writing results to: " + mServletTempDir);
        mInputURLs = requestUrls;
    }

    /**
     * Returns an Iterator to the List of URLs.
     */
    @Override
    public Iterator<List<URL>> getUrlIterator(InputSource source) {
        switch (source) {
        case NETWORK:
            return mInputURLs.iterator();
        default:
            mOutput.println("Invalid Source");
            return null;
        }
    }

    /**
     * Returns the path to the Servlet temp directory.
     */
    @Override
    public String getDirectoryPath() {
        return mServletTempDir;
    }

}
