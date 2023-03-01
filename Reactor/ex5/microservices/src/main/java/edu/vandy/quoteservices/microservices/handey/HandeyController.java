package edu.vandy.quoteservices.microservices.handey;

import edu.vandy.quoteservices.common.BaseController;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * This Spring controller demonstrates how Spring WebFlux can be used
 * to handle HTTP GET and POST requests asynchronously.
 *
 * The {@code @RestController} annotation is a specialization of
 * {@code @Component} and is automatically detected through classpath
 * scanning.  It adds the {@code @Controller} and {@code
 * @ResponseBody} annotations. It also converts responses to
 * JSON or XML.
 */
@RestController
public class HandeyController 
       extends BaseController<Flux<Quote>> {
}