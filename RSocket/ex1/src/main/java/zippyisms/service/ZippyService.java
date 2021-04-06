package zippyisms.service;

import org.springframework.beans.factory.annotation.Autowired;
import zippyisms.datamodel.ZippyQuote;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ZippyQuote> mQuotes;

    /**
     * @return The complete List of quotes from Zippy th' Pinhead.
     */
    public List<ZippyQuote> getmQuotes(){
        return mQuotes;
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
        assert mQuotes != null;
        // Subtract one since the List is 0-based.
        return mQuotes.get(id - 1);
    }

    /**
     * @return The total number of Zippy th' Pinhead quotes
     */
    public int getNumberOfQuotes() {
        return mQuotes.size();
    }
}
