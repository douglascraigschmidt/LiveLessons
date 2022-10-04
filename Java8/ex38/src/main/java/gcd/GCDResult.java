package gcd;

/**
 * Define a Java record that holds the "plain old data" (POD)
 * from computing the greatest common divisor (GCD).
 */
public record GCDResult(
    /*
     * A pair of {@link Integer} objects that were used to compute the
     * GCD.
     */
    GCDParam integers,

    /*
     * The value resulting from computing the GCD from the {@link
     * GCDParam} {@code integers}.
     */
    int gcd) 
{}

