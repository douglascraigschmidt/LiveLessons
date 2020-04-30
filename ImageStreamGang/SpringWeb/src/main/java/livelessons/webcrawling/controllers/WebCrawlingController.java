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

/**
 * Sanjeev, can you please add some comments here explaining what this
 * class does.
 */
@RestController
public class WebCrawlingController {
    /**
     * Sanjeev, can you please add some comments here explaining what
     * this field does.
     */
    @Autowired
    private WebCrawlingService webCrawlingService;
	
    /**
     * Iterates through all the implementation strategies to show how
     * the tests perform.
     */
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public ResponseEntity<String> run() {
        webCrawlingService.runTests();
        return new ResponseEntity<String>(HttpStatus.OK);
    }
	
    /**
     * Sanjeev, can you please add some comments here explaining what
     * this method does.
     */
    @RequestMapping(value = "/timingresults", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Long>> > getTimingResults() {
        Map<String, List<Long>> timingResults = webCrawlingService
            .getTimingResults();

        return new ResponseEntity<Map<String, List<Long>>>(timingResults,
                                                           HttpStatus.OK);
    }
	
     * Sanjeev, can you please add some comments here explaining what
     * this method does.
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<String> test() {
        return new ResponseEntity<String>("hello",
                                          HttpStatus.OK);
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
