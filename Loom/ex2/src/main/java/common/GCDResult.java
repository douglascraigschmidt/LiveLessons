package common;

/**
 * Define a Java {@code record} that holds the "plain old data" (POD)
 * from computing the greatest common divisor (GCD).
 */
public record GCDResult(/*
                         * The {@link GCDParam} to compute.
                         */
                        GCDParam param,

                        /*
                         * The value resulting from computing the GCD.
                         */
                        int gcd) {}

