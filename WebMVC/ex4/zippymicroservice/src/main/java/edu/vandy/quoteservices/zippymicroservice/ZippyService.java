package edu.vandy.quoteservices.zippymicroservice;

import edu.vandy.quoteservices.zippymicroservice.model.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link ZippyController}, which serves as the main "front-end"
 * app gateway entry point for remote clients that want to receive
 * movie recommendations.
 *
 * This class implements the abstract methods in {@link
 * ZippyService} using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ZippyService {
    /**
     * Spring-injected repository.
     */
    @Autowired
    ZippyRepository repository;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    public List<Quote> getAllQuotes() {
        System.out.println("ZippyService.getAllQuotes()");

        var list = repository
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
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        System.out.println("ZippyService.getQuotes()");

        var list = repository
            .findAllById(quoteIds);

        System.out.println("number of quotes = " + list.size());

        list.forEach(quote ->
                      System.out.println("id = " + quote.id + " quote = " + quote.quote));

        return list;
    }
}
