package edu.vandy.quoteservices.microservice.hippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static edu.vandy.quoteservices.common.Constants.ZIPPY_QUOTES;

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
     * Spring-injected repository.
     */
    @Autowired
    private ZippyRepository mRepository;

    private boolean mInitialized;

    ZippyService() {
        System.out.println("ZippyService constructor");
        // getInput(ZIPPY_QUOTES);
    }

    public void getInput(String filePath) {
        System.out.println("zippy.getInput() " + filePath);

        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicLong idCount = new AtomicLong(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource(filePath)
                .toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Get a List of Zippy objects.
            var list = Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new ZippyQuote.
                .map(quote ->
                     // new Quote(String.valueOf(idCount.incrementAndGet()),
                     new Quote(idCount.incrementAndGet(),
                               quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .toList();

            mRepository.saveAll(list);

            System.out.println("DATABASE: successfully loaded " 
                               + list.size() 
                               + " quotes.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    public List<Quote> getAllQuotes() {
        System.out.println("ZippyService.getAllQuotes()");

        if (!mInitialized) {
            getInput(ZIPPY_QUOTES);
        }

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

        if (!mInitialized) {
            getInput(ZIPPY_QUOTES);
        }

        var list = mRepository
            .findAllById(quoteIds);

        System.out.println("number of quotes = " + list.size());

        return list;
    }
}
