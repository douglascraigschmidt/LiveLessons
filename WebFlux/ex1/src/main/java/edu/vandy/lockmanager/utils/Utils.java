package edu.vandy.lockmanager.utils;

import java.util.UUID;

public class Utils {
    public static void log(String text) {
        var thread = Thread.currentThread(); //.threadId();
        System.out.println(text
            + " [" + thread + "]: ");
    }

    /**
     * @return A unique {@link String} id
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
