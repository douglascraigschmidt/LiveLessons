package livelessons.webcrawling.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import livelessons.ImageStreamGangTest;

@Service
public class WebCrawlingService {
	
	public void startCrawl() {
		runTests();
	}

	public void runTests() {
		ImageStreamGangTest.runTests();
		
	}
	
	public Map<String, List<Long>> getTimingResults(){
		return ImageStreamGangTest.getTimingResults();
	}
}
