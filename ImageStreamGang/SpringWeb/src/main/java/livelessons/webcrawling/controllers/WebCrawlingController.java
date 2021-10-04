package livelessons.webcrawling.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
     * injection. It can be used directly on properties, thereby
     * eliminating the need for getters and setters.
     */
    @Autowired
    private WebCrawlingService webCrawlingService;
	
    /**
     * The {@code @PostMapping} annotation is used to map HTTP POST
     * requests onto endpoint methods in Controller classes.
     * 
     * POST requests to /run endpoint maps to the run() endpoint
     * method.  When /run is invoked from any HTTP web client or by
     * using Curl on command line, this method will be invoked.
     * 
     * {@link ResponseEntity} represents an HTTP response, including
     * headers, body, and status.  It also adds headers and status
     * code.  This method returns the raw JSON response to the
     * clients.
     */
    @PostMapping("/run")
    public ResponseEntity<String> run() {
        webCrawlingService
            // Forward to the service.
            .runTests();

        // Return an "OK" response.
        return new ResponseEntity<String>(HttpStatus.OK);
    }
	
    /**
     * The {@code @GetMapping} annotation is used for mapping web
     * requests onto methods in Controller.  HTTP GET requests to the
     * "/timingresults" endpoint maps to this endpoint method. When
     * "/timingresults" is invoked from any HTTP web client or by
     * using Curl on command line this method will be called.
     * 
     * {@link ResponseEntity} represents an HTTP response, including
     * headers, body, and status.  It also adds headers and status
     * code.  This method returns the raw JSON response to the
     * clients.
     */
    @GetMapping("/timingresults")
    public ResponseEntity<Map<String, List<Long>> > getTimingResults() {
        Map<String, List<Long>> timingResults = webCrawlingService
            // Forward to the service.
            .getTimingResults();

        // Return the Map containing the timing results.
        return new ResponseEntity<Map<String, List<Long>>>(timingResults,
                                                           HttpStatus.OK);
    }
}
