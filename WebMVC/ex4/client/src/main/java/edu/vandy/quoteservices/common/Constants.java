package edu.vandy.quoteservices.common;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    // The Gateway's hostname and IP address.
    public static final String GATEWAY_BASE_URL = "http://localhost:8080";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_ALL_QUOTES = "allQuotes";
        public static final String POST_QUOTES = "quotes";
        public static final String POST_SEARCHES = "searchQuotes";
        public static final String POST_SEARCHES_EX = "searchQuotesEx";
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String ZIPPY = "zippy";
        public static final String HANDEY = "handey";
    }
}
