package quotes.common.model;

/**
 * This record contains a request for random {@link Quote} objects.
 */
public record RandomRequest (
    /*
     * The {@link Subscription} associated with this request.
     */
    Subscription subscription,

    /*
     * The array random quote indices.
     */
    Integer[] randomIndices)
    {}
