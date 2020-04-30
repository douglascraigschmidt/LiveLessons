package livelessons.webcrawling.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import livelessons.ImageStreamGangTest;

/**
 * Service Components are the class file that contains the @Service
 * annotation.  These class files are used to write business logic in
 * a different layer, separated from @RestController class file.
 */
@Service
public class WebCrawlingService {
    /**
     * This Service API forwards to runTests(), which runs all the
     * tests in ImageStreamGang.
     */
    public void startCrawl() {
        runTests();
    }

    /**
     * This Service API is used to run all tests in ImageStreamGang by
     * delegating to ImageStreamGangTest
     */
    public void runTests() {
        ImageStreamGangTest.runTests();
		
    }
	
    /**
     * This Service API is used to fetch timing results of last test
     * invocation.
     */
    public Map<String, List<Long>> getTimingResults(){
        return ImageStreamGangTest.getTimingResults();
    }
}
