package livelessons.webcrawling.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import livelessons.ImageStreamGangTest;

/**
 * Service Components are the class file that contains the Spring
 * {@code @Service} annotation.  These class files are used to write
 * business logic in a different layer, separated from the {@link
 * WebCrawlingController} class.
 */
@Service
public class WebCrawlingService {
    /**
     * This Service API is used to run all tests in the
     * ImageStreamGang app by delegating to {@link
     * ImageStreamGangTest}.
     */
    public void runTests() {
        // Run all the tests.
        ImageStreamGangTest.runTests();
    }
	
    /**
     * This Service API is used to fetch timing results of the last
     * test invocation.
     */
    public Map<String, List<Long>> getTimingResults(){
        // Return the timing results.
        return ImageStreamGangTest.getTimingResults();
    }
}
