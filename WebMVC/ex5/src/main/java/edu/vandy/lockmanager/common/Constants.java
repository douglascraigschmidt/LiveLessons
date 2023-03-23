package edu.vandy.lockmanager.common;

/**
 * Constants shared by the client and server components.
 */
public class Constants {
    public static final String LOCK_MANAGER_SERVER_BASE_URL = "http://localhost:8080";

    public static class Endpoints {
        public static final String CREATE = "create";
        public static final String ACQUIRE_LOCK = "acquireLock";
        public static final String RELEASE_LOCK = "releaseLock";
        public static final String ACQUIRE_LOCKS = "acquireLocks";
        public static final String RELEASE_LOCKS = "releaseLocks";
    }
}
