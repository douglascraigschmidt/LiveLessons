package utils;

/**
 * Represents a function that accepts three generic arguments and
 * produces a result.  This functional interface's abstract method is
 * {@link #apply(P1, P2, P3)}.
 */
@FunctionalInterface
public
interface TriFunction<P1, P2, P3, R> {
    /**
     * Apply the three generic parameters and return an instance of
     * {@code R}.
     */
    R apply(P1 p1, P2 p2, P3 p3);
}
