package edu.vandy.quoteservices.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Java utility class provides static methods for converting an
 * array {@link Object} to a {@link List} of subclasses of {@link
 * Number}, converting an {@link Object} to a subclass of {@link
 * Number}, and converting a {@link String} to a subclass of {@link
 * Number}.
 *
 * These methods use modern Java streams and lambda expressions to
 * perform the conversion and handle conversion failures by returning
 * null or an empty List. The class is useful when frequent
 * conversions are needed between {@link Object} objects and
 * subclasses of {@link Number}.
 */
public class ArrayUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private ArrayUtils() {}

    /**
     * Attempt to convert the {@link Object} param into a {@link List}
     * of {@link Class<T>}, which must be a subclass of {@link Number}
     * (e.g., {@link Integer}, {@link Double}, etc.
     *
     * @param obj The array {@link Object} to attempt to convert
     * @param clazz The type to attempt to convert into
     * @return A {@link List} of {@link Class<T>}, which must be a
     *         subclass of {@link Number} (e.g., {@link Integer},
     *         {@link Double}, etc. or an empty {@link List} if 
     *         the conversion fails
     * @param <T> A subclass of {@link Number}
     */
    public static <T extends Number> List<T> obj2Vector(Object obj,
                                                        Class<T> clazz) {
        if (obj == null || !obj.getClass().isArray()) {
            // Return an empty List on failure.
            return Collections.emptyList();
        } else {
            return Arrays
                // Convert the 'obj' array into a Stream.
                .stream((Object[]) obj)

                // Map each Object to its String representation.
                .map(Object::toString)

                // Try to convert each string to a subclass of Number.
                .map(string -> convertToNumber(string, clazz))

                // Ignore failure.
                .filter(Objects::nonNull)

                // Cast each remaining element to the type T specified
                // by the clazz argument
                .map(clazz::cast)

                // Convert the Stream to a List.
                .toList();
        }
    }

    /**
     * Converts an {@link Object} to a {@link Number} subclass of type
     * {@code T}.
     *
     * @param obj The {@link Object} to convert
     * @param clazz The {@link Class} object representing the desired {@link Number} subclass
     * @return A {@link Number} of type {@code T} or {@code null} if
     *         the conversion fails or the input {@link Object} is
     *         {@code null}
     */
    public static <T extends Number> T obj2Number
        (Object obj,
         Class<T> clazz) {
        // Bail out if 'obj' is null.
        if (obj == null) 
            return null;
        else {
            return Stream
                // Create a Stream containing the input Object
                .of(obj)

                // Convert the Object to its String representation.
                .map(Object::toString)

                // Attempt to convert the String to the desired Number
                // subclass.
                .map(str -> convertToNumber(str, clazz))

                // Remove any null values from the Stream
                .filter(Objects::nonNull)

                // Cast the remaining element to the desired Number
                // subclass.
                .map(clazz::cast)

                // Retrieve the first element of the Stream or null if
                // the Stream is empty. 
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Converts a {@link String} to a {@link Number} subclass of type
     * {@code T}.
     *
     * @param str The {@link String} to convert
     * @param clazz The {@link Class} object representing the desired
     *              {@link Number} subclass
     * @return A {@link Number} subclass of type {@code T} or {@code
     *         null} if the conversion fails
     */
    private static <T extends Number> T convertToNumber
        (String str,
         Class<T> clazz) {
        try {
            // Attempt to convert the input String to the desired
            // Number subclass using the valueOf() method of the
            // appropriate wrapper class. The clazz.cast() method is
            // used to cast the resulting object to the desired Number
            // subclass.
            return switch (clazz.getSimpleName()) {
                case "Integer" -> clazz.cast(Integer.valueOf(str));
                case "Long" -> clazz.cast(Long.valueOf(str));
                case "Float" -> clazz.cast(Float.valueOf(str));
                case "Double" -> clazz.cast(Double.valueOf(str));
                case "Short" -> clazz.cast(Short.valueOf(str));
                case "Byte" -> clazz.cast(Byte.valueOf(str));
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
