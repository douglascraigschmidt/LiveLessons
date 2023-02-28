package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.POST_SEARCHES_EX;

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
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private JPAQuoteRepository mRepository;
}
