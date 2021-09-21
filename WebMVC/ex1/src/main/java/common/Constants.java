package common;

/**
 * Static class used to centralize all constants used by this project.
 */
public class Constants {
    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String CHECK_IF_PRIME = "/checkIfPrime";
        public static final String CHECK_IF_PRIME_LIST = "/checkIfPrimeList";
    }

    /**
     * Common resource file names used by all microservices,
     * which reside in the {@code src/main/resources} folder.
     */
    public static class Resources {
        public static final String SERVER_PROPERTIES =
            "classpath:/server/server-application.properties";
    }
}
