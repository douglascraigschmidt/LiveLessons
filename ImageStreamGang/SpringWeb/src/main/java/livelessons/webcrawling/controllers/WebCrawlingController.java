package livelessons.webcrawling.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import livelessons.webcrawling.services.WebCrawlingService;


@RestController
public class WebCrawlingController {
	@Autowired
	private WebCrawlingService webCrawlingService;
	
	
	 /**
     * Iterates through all the implementation strategies to test how
     * tests perform.
     */
	@RequestMapping(value = "/run", method = RequestMethod.POST)
	public ResponseEntity<String> run() {
		webCrawlingService.runTests();
	    return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	/**
     * Print out all the timing results for all the test runs in order
     * from fastest to slowest.
     */
	@RequestMapping(value = "/timingresults", method = RequestMethod.GET)
	public ResponseEntity<Map<String, List<Long>> > getTimingResults() {
		Map<String, List<Long>> timingResults=webCrawlingService.getTimingResults();
	    return new ResponseEntity<Map<String, List<Long>>>(timingResults,HttpStatus.OK);
	}
	
	@GetMapping("/test")
	@ResponseBody
	public ResponseEntity<String> test(){
		
		return new ResponseEntity<String>("hello", HttpStatus.OK);
		
	}
	
	/*@RequestMapping(value = "/crawl", method = RequestMethod.POST)
	public ResponseEntity<List<String>> startCrawling(@RequestParam String urlPrefix, @RequestBody List<String> urls) {
		Options.instance().setInputSource(InputSource.DEFAULT);
		Options.instance().setURLPrefix(urlPrefix);
		Options.instance().setDefaultImageNames(urls);
		
		webCrawlingService.startCrawl();
	    return new ResponseEntity<List<String>>(urls, HttpStatus.OK);
	}
	*/
	
	
}
