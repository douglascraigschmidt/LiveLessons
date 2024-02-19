package edu.vandy.quoteservices.common;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    public static final String HANDEY_QUOTES = "handey/handey-quotes.txt";
    public static final String ZIPPY_CACHE = "ZippyCache";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_ALL_QUOTES = "getAllQuotes";
        public static final String GET_QUOTE = "getQuote";
        public static final String POST_QUOTES = "postQuotes";
        public static final String POST_SEARCHES = "postSearchQuotes";
        public static final String POST_SEARCHES_EX = "postSearchQuotesEx";
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String ZIPPY = "zippy";
        public static final String HANDEY = "handey";
    }
}
