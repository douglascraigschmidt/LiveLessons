package berraquotes.common;

/**
 * This class centralizes all constants used by the Berra
 * microservices.
 */
public class Constants {
    public static final String SERVER_BASE_URL = "http://localhost:9102";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_ALL_QUOTES = "all_quotes";
        public static final String GET_QUOTES = "quotes";
        public static final String GET_SEARCH = "search-quote";
        public static final String SEARCH_QUERY = "/{query}";

        /**
         * Supported HTTP request parameters identifiers.
         */
        public static class Params {
            public static final String QUOTE_IDS_PARAM = "quoteIds";
        }
    }
}
