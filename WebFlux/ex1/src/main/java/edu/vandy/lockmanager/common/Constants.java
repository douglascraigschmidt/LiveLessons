package edu.vandy.lockmanager.common;

/**
 * Constants shared by the client and server components.
 */
public interface Constants {
	String SERVER_BASE_URL = "http://localhost:8080";

	public interface Endpoints {
		String CREATE = "create", ACQUIRE_LOCK = "acquireLock", ACQUIRE_LOCKS = "acquireLocks",
				RELEASE_LOCK = "releaseLock", RELEASE_LOCKS = "releaseLocks", ACQUIRE_LOCKS_TEST = "acquireLocksTest";
	}
}
