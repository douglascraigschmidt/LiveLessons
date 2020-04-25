package utils;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A utility class containing helpful methods for manipulating various
 * Java Streams features.
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
     * @return A CompletableFuture to a list that will contain all the
     *         joined results.
     */
    public static <T> CompletableFuture<List<T>> 
        joinAllList(List<CompletableFuture<T>> futureList) {
        // Use CompletableFuture.allOf() to obtain a CompletableFuture
        // that will itself be complete when all CompletableFutures in
        // futureList parameter have completed.
        CompletableFuture<Void>
            allDoneFuture = CompletableFuture.allOf
            (futureList.toArray(new CompletableFuture[futureList.size()]));

        // When all futures have completed return a CompletableFuture to
        // a list of joined elements of type T.
        return allDoneFuture
            .thenApply(v -> futureList
                       // Convert futureList into a stream of
                       // completable futures.
                       .stream()

                       // Use map() to join() all completable futures
                       // and yield objects of type T.  Note that
                       // join() should never block.
                       .map(CompletableFuture::join)

                       // Collect the results of type T into a list.
                       .collect(toList()));
    }

    /**
     * Create a CompletableFuture that, when completed, will convert
     * all the completed CompletableFutures in the {@code futureStream}
     * parameter into a list of joined results.
     *
     * @param futureStream A stream of completable futures
     * @return A CompletableFuture to a list that will contain all the
     *         joined results.
     */
    public static <T> CompletableFuture<List<T>>
    joinAllList(Stream<CompletableFuture<T>> futureStream) {
        // Create an array of CompletableFutures from the futureStream
        // param.
        CompletableFuture<T>[] futures =
                futureStream.toArray(CompletableFuture[]::new);

        // Use CompletableFuture.allOf() to obtain a CompletableFuture
        // that will itself be complete when all CompletableFutures in
        // futureStream parameter have completed.
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures);

        // When all futures have completed return a CompletableFuture to
        // a list of joined elements of type T.
        return allDoneFuture
                .thenApply(v -> Arrays
                        // Convert futures into a stream of completable
                        // futures.
                        .stream(futures)

                        // Use map() to join() all completable futures
                        // and yield objects of type T.  Note that
                        // join() should never block.
                        .map(CompletableFuture::join)

                        // Collect the results of type T into a list.
                        .collect(toList()));
    }

    /**
     * Create a CompletableFuture that, when completed, will convert
     * all the completed CompletableFutures in the @a futureList
     * parameter into a list of joined results.
     *
     * @param futureList A list of completable futures.
     * @return A CompletableFuture to a stream that will contain all the
     *         joined results.
     */
    public static <T> CompletableFuture<Stream<T>>
        joinAllStream(List<CompletableFuture<T>> futureList) {
        // Use CompletableFuture.allOf() to obtain a CompletableFuture
        // that will itself be complete when all CompletableFutures in
        // futureList parameter have completed.
        CompletableFuture<Void>
            allDoneFuture = CompletableFuture.allOf
            (futureList.toArray(new CompletableFuture[futureList.size()]));

        // When all futures have completed return a CompletableFuture to
        // a list of joined elements of type T.
        return allDoneFuture
            .thenApply(v -> futureList
                       // Convert futureList into a stream of
                       // completable futures.
                       .stream()

                       // Use map() to join() all completable futures
                       // and yield objects of type T.  Note that
                       // join() should never block.
                       .map(CompletableFuture::join));
    }

    /**
     * Create a CompletableFuture that, when completed, will convert
     * all the completed CompletableFutures in the {@code futureStream}
     * parameter into a list of joined results.
     *
     * @param futureStream A stream of completable futures
     * @return A CompletableFuture to a stream that will contain all the
     *         joined results.
     */
    public static <T> CompletableFuture<Stream<T>>
        joinAllStream(Stream<CompletableFuture<T>> futureStream) {
        // Create an array of CompletableFutures from the futureStream
        // param.
        CompletableFuture<T>[] futures =
            futureStream.toArray(CompletableFuture[]::new);

        // Use CompletableFuture.allOf() to obtain a CompletableFuture
        // that will itself be complete when all CompletableFutures in
        // futureStream parameter have completed.
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures);

        // When all futures have completed return a CompletableFuture to
        // a list of joined elements of type T.
        return allDoneFuture
            .thenApply(v -> Arrays
                       // Convert futures into a stream of completable
                       // futures.
                       .stream(futures)

                       // Use map() to join() all completable futures
                       // and yield objects of type T.  Note that
                       // join() should never block.
                       .map(CompletableFuture::join));
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
     * Concatenate {@code secondList} at the end of {@code firstList}
     * and return the updated {@code firstList}.
     *
     * @return An updated {@code firstList} with the contents of
     *         {@code secondList} concatenated at the end
     */
    public static<T> ArrayList<T> concat(ArrayList<T> firstCol,
                                         ArrayList<T> secondCol) {
        // Add secondList to the end of firstList.
        firstCol.addAll(secondCol);

        // Return the update firstList.
        return firstCol;
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
}
