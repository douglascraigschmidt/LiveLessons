package livelessons.webcrawling.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import livelessons.ImageStreamGangTest;

/**
 * Sanjeev, can you please add some comments here explaining what this
 * class does.
 */
@Service
public class WebCrawlingService {
    /**
     * Sanjeev, can you please add some comments here explaining what this
     * method does.
     */
    public void startCrawl() {
        runTests();
    }

    /**
     * Sanjeev, can you please add some comments here explaining what this
     * method does.
     */
    public void runTests() {
        ImageStreamGangTest.runTests();
		
    }
	
    /**
     * Sanjeev, can you please add some comments here explaining what this
     * method does.
     */
    public Map<String, List<Long>> getTimingResults(){
        return ImageStreamGangTest.getTimingResults();
    }
}
