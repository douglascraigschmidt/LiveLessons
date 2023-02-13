package common;

/**
 * Define a Java {@code record} that holds the "plain old data" (POD)
 * parameters needed to compute the greatest common divisor (GCD).
 */
public record GCDParam(
    /*
     * The first int to process.
     */
    int int1,

    /*
     * The second int to process.
     */
    int int2) {}

