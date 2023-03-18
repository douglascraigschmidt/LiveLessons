package edu.vandy.pubsub.common;

public class Constants {
    public static final String SERVER_BASE_URL = "http://localhost:8080";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String GET_START = "start";
        public static final String DELETE_STOP = "stop";
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String PUBLISHER = "publisher";
    }
}
