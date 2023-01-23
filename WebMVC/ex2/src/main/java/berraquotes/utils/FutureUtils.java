package berraquotes.utils;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * A Java utility class that provides helper methods for dealing with
 * Java {@link Future} objects.
 */
public final class FutureUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private FutureUtils() {}

    /**
     * Convert a {@link List} of {@link Future<T>} objects to a
     * {@link List} of {@code T} objects.
     *
     * @param list The {@link List} of {@link Future<T>} objects
     * @return A {@link List} of {@code T} objects
     */
    public static <T> List<T> futures2List(List<Future<T>> list) {
        return list
            // Convert the List to a Stream.
            .stream()

            // Map the Future<Integer> to Integer.
            .map(Future::resultNow)

            // Convert the Stream to a List.
            .toList();
    }


    /**
     * Convert a {@link List} of {@link Future<T>} objects to a
     * {@link Stream} of {@code T} objects.
     *
     * @param list The {@link List} of {@link Future<T>} objects
     * @return A {@link Stream} of {@code T} objects
     */
    public static <T> Stream<T> futures2Stream(List<Future<T>> list) {
        return list
                // Convert the List to a Stream.
                .stream()

                // Map the Future<Integer> to Integer.
                .map(Future::resultNow);
    }
}
