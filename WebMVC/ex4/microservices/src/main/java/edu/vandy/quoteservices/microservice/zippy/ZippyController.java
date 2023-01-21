package edu.vandy.quoteservices.microservice.zippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This Spring controller demonstrates how Spring WebMVC can be used
 * to handle HTTP GET requests.
 *
 * The {@code @RestController} annotation is a specialization of
 * {@code @Component} and is automatically detected through classpath
 * scanning.  It adds the {@code @Controller} and
 * {@code @ResponseBody} annotations. It also converts responses to
 * JSON or XML.
 */
@RestController
public class ZippyController
    extends BaseController<List<Quote>> {
}
