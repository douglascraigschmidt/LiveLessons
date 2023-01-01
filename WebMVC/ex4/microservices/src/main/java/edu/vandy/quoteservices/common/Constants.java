package edu.vandy.quoteservices.common;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    public static final String HANDEY_QUOTES = "handey/handey-quotes.txt";
    public static final String ZIPPY_QUOTES = "zippy/zippy-quotes.txt";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_ALL_QUOTES = "all-quotes";
        public static final String GET_QUOTES = "quotes";

        /**
         * Supported HTTP request parameters identifiers.
         */
        public static class Params {
            public static final String QUOTE_IDS_PARAM = "quoteIds";
        }
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String ZIPPY = "zippy";
        public static final String HANDEY = "handey";
    }
}
