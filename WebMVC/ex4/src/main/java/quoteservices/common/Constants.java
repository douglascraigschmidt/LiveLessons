package quoteservices.common;

/**
 * Static class used to centralize all constants used by this project.
 */
public class Constants {
    public static final String ZIPPY_MICROSERVICE_BASE_URL = "http://localhost:8081";
    public static final String HANDEY_MICROSERVICE_BASE_URL = "http://localhost:8082";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_ZIPPY_QUOTE = "/getZippyQuote";
        public static final String GET_HANDEY_QUOTE = "/getHandeyQuote";
    }
}
