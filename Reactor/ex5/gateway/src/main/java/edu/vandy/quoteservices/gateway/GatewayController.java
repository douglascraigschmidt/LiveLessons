package edu.vandy.quoteservices.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A common controller implementation that redirects all requests to
 * custom microservices (e.g., HandeyApplication and ZippyApplication)
 * that perform the requested service.
 */
@RestController
public class GatewayController {
    /**
     * Application id is returned for this single endpoint.
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * An endpoint for testing the alive state of this application.
     *
     * @return The application name.
     */
    @GetMapping({"/", "/actuator/info"})
    ResponseEntity<String> info() {
        // Indicate the request succeeded and return the application
        // name.
        return ResponseEntity
            .ok(applicationContext.getId() 
                + " is alive and running on"
                + Thread.currentThread()
                + "\n");
    }
}
