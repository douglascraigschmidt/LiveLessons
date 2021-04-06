package zippyisms.utils;

/**
 * Defines constants used through the program.
 */
public class Constants {
    /**
     * Port number listened upon by the controller.
     */
    public static final int SERVER_PORT = 10200;

    /*
     * These constants identify RSocket endpoint names.
     */
    public static final String SUBSCRIBE = "subscription.subscribe";
    public static final String CANCEL = "subscription.cancel";
    public static final String GET_QUOTES = "zippyisms.getQuotes";
    public static final String GET_QUOTE = "zippyisms.getQuote";
    public static final String GET_NUMBER_OF_QUOTES = "zippyisms.getNumberOfQuotes";
}

