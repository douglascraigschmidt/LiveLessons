package edu.vandy.mathservices.common;

/**
 * Define a Java {@code record} that holds the "plain old data" (POD)
 * from computing the greatest common divisor (GCD).
 */
public record GCDResult(
    /*
     * The first int to process.
     */
    int int1,

    /*
     * The second int to process.
     */
    int int2,

    /*
     * The value resulting from computing the GCD.
     */
    int gcd) {}

