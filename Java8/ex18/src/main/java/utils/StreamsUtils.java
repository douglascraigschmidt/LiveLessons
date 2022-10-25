package utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class contains methods for manipulating various
 * modern Java Streams features.
 */
public class StreamsUtils {
    /**
     * A Java utility class should always define a private constructor.
     */
    private StreamsUtils() {
    }

    /**
     * Create a CompletableFuture that when completed will convert all
     * the completed CompletableFutures in the {@code futures}
     * parameter into a list of results.
     *
     * @param futures A {@link List} of {@link CompletableFuture}
     *                objects
     * @return A {@link CompletableFuture} containing a {@link List}
     *         with all the joined results
     */
    public static <T> CompletableFuture<List<T>> joinAll
        (List<CompletableFuture<T>> futures) {
        // Obtain a CompletableFuture that will be complete when all
        // the CompletableFuture objects have completed.
        CompletableFuture<Void> allDoneFuture = CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]));

        // When all the futures have completed return a list of the
        // joined elements.
        return allDoneFuture
            .thenApply(v -> futures
                       // Convert the List into a Stream.
                       .stream()

                       // Join all CompletableFuture objects and yield
                       // objects of type T (join() will never block).
                       .map(CompletableFuture::join)

                       // Collect the results of type T into a List.
                       .toList());
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
}
