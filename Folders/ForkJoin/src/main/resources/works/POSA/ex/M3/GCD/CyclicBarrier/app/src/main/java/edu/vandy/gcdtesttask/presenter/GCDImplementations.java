package edu.vandy.gcdtesttask.presenter;

import java.math.BigInteger;

/**
 * This utility class defines various Greatest Common Divisor (GCDInterface) algorithm
 * implementations from https://rosettacode.org/wiki/Greatest_common_divisor#Java.
 */
public class GCDImplementations {
    /**
     * Ensure this class is only used as a utility.
     */
    private GCDImplementations() {
        throw new AssertionError();
    }

    /**
     * Compute the GCDInterface of parameters @a and @b using the iterative
     * Euclid algorithm.
     */
    public static int computeGCDIterativeEuclid(int a,
                                                int b) {
        while (b > 0) {
            int c = a % b;
            a = b;
            b = c;
        }
        return a;
    }

    /**
     * Compute the GCDInterface of parameters @a and @b using the recursive
     * Euclid algorithm.
     */
    public static int computeGCDRecursiveEuclid(int a,
                                                int b) {
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        if (a > b) {
            return computeGCDRecursiveEuclid(b,
                                             a % b);
        }
        return
            computeGCDRecursiveEuclid(a,
                                      b % a);
    }

    /**
     * Compute the GCDInterface of parameters @a and @b using the BigInteger
     * algorithm.
     */
    public static int computeGCDBigInteger(int a,
                                           int b) {
        return BigInteger.valueOf(a)
            .gcd(BigInteger.valueOf(b))
            .intValue();
    }

    /**
     * Compute the GCDInterface of parameters @a and @b using the Stein binary
     * algorithm.
     */
    public static int computeGCDBinary(int a,
                                       int b) {
        int t, k;

        if (b == 0) {
            return a;
        }

        if (a < b) {
            t = a;
            a = b;
            b = t;
        }

        for (k = 1; (a & 1) == 0 && (b & 1) == 0; k <<= 1) {
            a >>= 1;
        }
        b >>= 1;

        t = (a & 1) != 0 ? -b : a;

        while (t != 0) {
            while ((t & 1) == 0) {
                t >>= 1;
            }

            if (t > 0) {
                a = t;
            } else {
                b = -t;
            }

            t = a - b;
        }

        return a * k;
    }
}
