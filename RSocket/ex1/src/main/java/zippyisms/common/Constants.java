package zippyisms.common;

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
    public static final String SUBSCRIBE = "zippycontroller.subscribe";
    public static final String CANCEL_CONFIRMED = "zippycontroller.cancelConfirmed";
    public static final String CANCEL_UNCONFIRMED = "zippycontroller.cancelUnconfirmed";
    public static final String GET_ALL_QUOTES = "zippycontroller.getQuotes";
    public static final String GET_RANDOM_QUOTES = "zippycontroller.getQuote";
    public static final String GET_NUMBER_OF_QUOTES = "zippycontroller.getNumberOfQuotes";
}

