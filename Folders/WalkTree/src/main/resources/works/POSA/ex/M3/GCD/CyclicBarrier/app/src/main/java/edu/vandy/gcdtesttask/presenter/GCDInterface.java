package edu.vandy.gcdtesttask.presenter;

/**
/**
 * This functional interface matches the signature of all the GCD
 * implementation methods.
 */
@FunctionalInterface
public interface GCDInterface {
    /**
     * Compute and return the GCD for parameters @a a and @a b.
     */
    int compute(int a,
                int b);
}
