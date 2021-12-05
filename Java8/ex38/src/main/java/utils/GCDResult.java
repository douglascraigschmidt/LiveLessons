package utils;

/**
 * Define a Java record that holds the "plain old data" (POD)
 * from computing the greatest common divisor (GCD).
 */
public record GCDResult(
    /*
     * The two integers to process.
     */
    GCDParam integers,

    /*
     * The value resulting from computing the GCD.
     */
    Integer gcd) {}

