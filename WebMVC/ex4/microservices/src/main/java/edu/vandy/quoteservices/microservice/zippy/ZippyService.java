package edu.vandy.quoteservices.microservice.zippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.common.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive movie
 * recommendations.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ZippyService
       extends BaseService<List<Quote>> {
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private QuoteRepository mRepository;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    public List<Quote> getAllQuotes() {
        System.out.println("ZippyService.getAllQuotes()");

        var list = mRepository
            // Forward to the repository.
            .findAll();

        System.out.println("number of quotes = " + list.size());

        return list;
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Long> quoteIds) {
        System.out.println("ZippyService.getQuotes()");

        var list = mRepository
            .findAllById(quoteIds);

        System.out.println("number of quotes = " + list.size());

        return list;
    }
}