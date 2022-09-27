package folders.common;

/**
 * Static class used to centralize all constants used by this project.
 */
public class Constants {
    public static final String SERVER_BASE_URL = "http://localhost:8081";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String FOLDERS = "/folders";
        public static final String ROOT_DIR = "/works";
        public static final String PATH = "/{rootDir}";
        public static final String SEARCH = "/searchFolder";
        public static final String GET_DOCUMENTS = "/getDocuments";
        public static final String COUNT_DOCUMENTS = "/countDocuments";
        public static final String COUNT_LINES = "/countLines";
        public static final String CREATE_FOLDER = "/createFolder";
    }
}

