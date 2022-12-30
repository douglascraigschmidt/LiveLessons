package edu.vandy.mathservices.common;

/**
 * Static class used to centralize all constants used by this project.
 */
public class Constants {
    public static final String GCD_MICROSERVICE_BASE_URL = "http://localhost:8081";
    public static final String PRIMALITY_MICROSERVICE_BASE_URL = "http://localhost:8082";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String COMPUTE_GCD_LIST = "/computeGCDList";
        public static final String CHECK_PRIMALITY_LIST = "/checkPrimalityList";
    }
}
