package zippyisms.service;

import zippyisms.datamodel.ZippyQuote;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This service returns quotes from Zippy th' Pinhead.
 */
@Service
public class ZippyService {
    /**
     * An in-memory list of all quotes from Zippy th' Pinhead.
     */
    public final static List<ZippyQuote> quotes = getInput();

    /**
     * @return Return the file of Zippyisms as a list of ZippyQuote
     * objects.
     */
    public static List<ZippyQuote> getInput() {
        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader.getSystemResource("zippyisms.txt").toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new ZippyQuote.
                .map(quote ->
                         new ZippyQuote(idCount.incrementAndGet(),
                                        quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return The complete List of quotes from Zippy th' Pinhead.
     */
    public List<ZippyQuote> getQuotes(){
        return quotes;
    }

    /**
     * Returns a specific quote from Zippy th' Pinhead based on the
     * given {@code id}.
     *
     * @param id The requested {@code id}
     * @return The quote associated iwth the requested {@code id}
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    public ZippyQuote getQuote(int id){
        assert quotes != null;
        // Subtract one since the List is 0-based.
        return quotes.get(id - 1);
    }

}
