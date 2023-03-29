package edu.vandy.lockmanager.utils;

public class Logger {
    public static void log(String text) {
        var thread = Thread.currentThread(); //.threadId();
        System.out.println(text
        + " [" + thread + "]");
    }
}
