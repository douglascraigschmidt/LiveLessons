package livelessons.webcrawling.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import livelessons.webcrawling.services.WebCrawlingService;

/**
 * In Spring’s approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operations
 * (@GetMapping, @PostMapping, @PutMapping and @DeleteMapping,
 * corresponding to HTTP GET, POST, PUT, and DELETE calls).  These
 * components are identified by the @RestController annotation below.
 */
@RestController
public class WebCrawlingController {
    /**
     * The @Autowired annotation is used for automatic dependency
     * injection. It can be used directly on properties, therefore
     * eliminating the need for getters and setters:
     */
    @Autowired
    private WebCrawlingService webCrawlingService;
	
    /**
     * @RequestMapping annotation is used for mapping web requests onto methods in Controller classes. 
     * 
     * POST requests to /run endpoint maps to method run(.  When /run
     * is invoked from any HttpWebClient or by using Curl on command
     * line, this method will be invoked.
     * 
     * ResponseEntity represents an HTTP response, including headers,
     * body, and status.  While @ResponseBody puts the return value
     * into the body of the response, ResponseEntity also allows us to
     * add headers and status code.  This method returns the raw JSON
     * response to the clients.
     */
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public ResponseEntity<String> run() {
        webCrawlingService.runTests();
        return new ResponseEntity<String>(HttpStatus.OK);
    }
	
    /**
     * @RequestMapping annotation is used for mapping web requests
     * onto methods in Controller.  GET requests to /timingresults
     * endpoint maps to method getTimingResults(). When /timingresults
     * is invoked from any HttpWebClient or by using Curl on command
     * line this method will be called.
     * 
     * ResponseEntity represents an HTTP response, including headers,
     * body, and status.  While @ResponseBody puts the return value
     * into the body of the response, ResponseEntity also allows us to
     * add headers and status code.  This method returns the raw JSON
     * response to the clients.
     * 
     */
    @RequestMapping(value = "/timingresults", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Long>> > getTimingResults() {
        Map<String, List<Long>> timingResults = webCrawlingService
            .getTimingResults();

        return new ResponseEntity<Map<String, List<Long>>>(timingResults,
                                                           HttpStatus.OK);
    }
}
