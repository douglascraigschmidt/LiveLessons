package zippyisms.service;

import org.springframework.beans.factory.annotation.Autowired;
import zippyisms.datamodel.ZippyQuote;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines methods that return zany quotes from Zippy th'
 * Pinhead.  It is annotated as a Spring @Service, which enables the
 * autodetection of implementation classes via classpath scanning (in
 * this case the ZippyQuotes).
 */
@Service
public class ZippyService {
    /**
     * An in-memory list of all quotes from Zippy th' Pinhead.  The
     * @Autowired annotation marks this field to be initialized via
     * Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, the
     * List of ZippyQuote objects from the ZippyQuotes class).
     */
    @Autowired
    public List<ZippyQuote> mQuotes;

    /**
     * @return The complete List of quotes from Zippy th' Pinhead
     */
    public List<ZippyQuote> getQuotes(){
        return mQuotes;
    }

    /**
     * Return a specific quote from Zippy th' Pinhead corresponding to
     * the given {@code quoteId}.
     *
     * @param quoteId The requested {@code quoteId}
     * @return The quote associated iwth the requested {@code quoteId}
     * @throws IndexOutOfBoundsException if {@code quoteId} is out of
     *         range (index < 0 || index >= size())
     */
    public ZippyQuote getQuote(int quoteId){
        // Subtract 1 since the List is 0-based.
        return mQuotes.get(quoteId - 1);
    }

    /**
     * @return The total number of Zippy th' Pinhead quotes
     */
    public int getNumberOfQuotes() {
        return mQuotes.size();
    }
}
