package example;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

public class PlatformStrategyProxy extends PlatformStrategyConsole {
	
	private String mServletTempDir;
	private List<List<URL>> mInputURLs;
	
	public PlatformStrategyProxy(Object output,
								 ServletContext servletContext, 
								 List<List<URL>> requestUrls) {
		super(output);
		mServletTempDir = servletContext.getAttribute(
				ServletContext.TEMPDIR).toString();
		mOutput.println("Writing results to: " + mServletTempDir);
		mInputURLs = requestUrls;
	}

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

	@Override
	public String getDirectoryPath() {
		return mServletTempDir;
	}

}
