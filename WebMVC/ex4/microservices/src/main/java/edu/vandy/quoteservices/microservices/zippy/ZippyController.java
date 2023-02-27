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

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List<Quote>} of matches using a custom
     * SQL query.
     *
     * @param queries The search queries
     * @return A {@code T} containing the queries
     */
    @PostMapping(POST_SEARCHES_EX)
    public List<Quote> searchEx(@RequestBody List<String> queries) {
        // Use a custom SQL query to find all movies whose 'id'
        // matches the List of 'queries' and return them as a List of
        // Quote objects that contain no duplicates.
        return mRepository
            .findAllByQuoteContainingAllIn(queries);
    }

}
