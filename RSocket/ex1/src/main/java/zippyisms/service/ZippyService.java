package zippyisms.service;

import org.springframework.beans.factory.annotation.Autowired;
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
 * This class defines methods that return zany quotes from Zippy th'
 * Pinhead.  It is annotated as a Spring @Service, which enables the
 * autodetection of implementation classes via classpath scanning.
 */
@Service
public class ZippyService {
    /**
     * An in-memory list of all quotes from Zippy th' Pinhead.
     */
    @Autowired
    public List<ZippyQuote> quotes;

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

    /**
     * @return The total number of Zippy th' Pinhead quotes
     */
    public int getNumberOfQuotes() {
        return quotes.size();
    }
}
