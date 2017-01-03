package livelessons.utils;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Helpful methods for manipulating various Java 8 Streams features.
 */
public class StreamsUtils {
    /**
     * A utility class should always define a private constructor.
     */
    private StreamsUtils() {
    }

    /**
     * Create a CompletableFuture that, when completed, will convert
     * all the completed CompletableFutures in the @a futureList
     * parameter into a list of joined results.
     *
     * @param futureList A list of completable futures.
     * @return A CompletableFuture to a list that will contain all the joined results.
     */
    public static <T> CompletableFuture<List<T>> joinAll(List<CompletableFuture<T>> futureList) {
        // Obtain a CompletableFuture that will itself be complete
        // when all CompletableFutures in futureList have completed.
        CompletableFuture<Void>
            allDoneFuture = CompletableFuture.allOf
            (fList.toArray(new CompletableFuture[f.size()]));

        // When all futures have completed get a CompletableFuture to
        // a list of joined elements.
        CompletableFuture<List<T>> allDoneList =
            allDoneFuture.thenApply(v -> futureList.stream()
                              .map(CompletableFuture::join)
                              .collect(toList()));

        // Return the CompletableFuture. 
        return allDoneList;
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
     * A generic negation predicate that can be used to negate the
     * return value of urlCached (used by Collection.filter() calls).
     *
     * @return The negation of the input predicate.
     */
    public static<T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }
}
