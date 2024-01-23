package utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.concurrent.StructuredTaskScope;

/**
 * A Java utility class that provides helper methods for dealing with
 * Java {@link StructuredTaskScope.Subtask} objects that extend {@link
 * Supplier}.
 */
public final class SupplierUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private SupplierUtils() {}

    /**
     * Convert a {@link List} of {@link Supplier<T>} objects to a
     * {@link List} of {@code T} objects.
     *
     * @param list The {@link List} of {@link Supplier<T>} objects
     * @return A {@link List} of {@code T} objects
     */
    public static <T> List<T> suppliers2Objects(List<Supplier<T>> list) {
        return list
            // Convert the List to a Stream.
            .stream()

            // Map the Supplier<Integer> to Integer.
            .map(Supplier::get)

            // Remove any nulls.
            .filter(Objects::nonNull)

            // Convert the Stream to a List.
            .toList();
    }
}
