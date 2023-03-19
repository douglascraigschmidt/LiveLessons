package edu.vandy.lockmanager;

public class Utils {
    public static void log(String text) {
        var thread = Thread.currentThread(); //.threadId();
        System.out.println(text
        + " [" + thread + "]: ");
    }
}
