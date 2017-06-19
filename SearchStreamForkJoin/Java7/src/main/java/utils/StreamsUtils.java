package utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods for manipulating various Java 8
 * Streams features.
 */
public class StreamsUtils {
    /**
     * A utility class should always define a private constructor.
     */
    private StreamsUtils() {
    }

    /**
     * Create a CompletableFuture that when completed will convert all
     * the completed CompletableFutures in the @a futures parameter
     * into a list of results.
     * @param futures A list of completable futures.
     * @return A CompletableFuture containing a List with all the joined results.
     */
    public static <T> CompletableFuture<List<T>> joinAll(List<CompletableFuture<T>> futures) {
        // Obtain a CompletableFuture that will be complete when all
        // of the futures have completed.
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));

        // When all the futures have completed return a list of the
        // joined elements.
        return allDoneFuture.thenApply(v ->
                                       futures
                                       .stream()
                                       .map(CompletableFuture::join)
                                       .collect(toList()));
    }

    /**
     * Maps the values of an Enum type to a corresponding array of
     * Strings.
     */
    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays
            .stream(e.getEnumConstants())
            .map(Enum::name)
            .toArray(String[]::new);
    }

    /**
     * A generic negation predicate that can be used to negate a
     * predicate.
     *
     * @return The negation of the input predicate.
     */
    public static<T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    /**
     * Represents a function that accepts five arguments and produces
     * a result.  This is a functional interface whose functional
     * method is
     * {@link #apply(Object, Object, Object, Object, Object)}.
     */
    @FunctionalInterface
    public interface PentaFunction<P1,
                                   P2,
                                   P3,
                                   P4,
                                   P5,
                                   R> {
        /**
         * Apply the parameters and return a value.
         */
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    /**
     * Represents a function that accepts four arguments and produces
     * a result.  This is a functional interface whose functional
     * method is
     * {@link #apply(Object, Object, Object, Object)}.
     */
    @FunctionalInterface
    public interface QuadFunction<P1,
                                  P2,
                                  P3,
                                  P4,
                                  R> {
        /**
         * Apply the parameters and return a value.
         */
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }
}
