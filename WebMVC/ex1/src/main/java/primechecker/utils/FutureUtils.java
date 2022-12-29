package primechecker.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

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
    public static <T> List<T> futures2Objects(List<Future<T>> list) {
        return list
            // Convert the List to a Stream.
            .stream()

            // Map the Future<Integer> to Integer.
            .map(Future::resultNow)

            // Remove any nulls.
            .filter(Objects::nonNull)

            // Convert the Stream to a List.
            .toList();
    }
}
