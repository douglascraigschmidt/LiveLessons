package edu.vandy.lockmanager.utils;

import java.util.UUID;
import java.util.logging.Logger;

public interface Utils {

	Logger logger = Logger.getLogger(Utils.class.getName());

	static void log(String text) {
		var thread = Thread.currentThread(); // .threadId();
		logger.info(text + " [" + thread + "]: ");
	}

	/**
	 * @return A unique {@link String} id
	 */
	static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
}
