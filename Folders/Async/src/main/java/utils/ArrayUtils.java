package utils;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains methods that perform Array and List operations.
 */
public class ArrayUtils {
    /**
     * Concatenate the contents of two arrays and return the result as
     * an array.
     *
     * @return The concatenated contents of two arrays as a single
     * array or null if both arrays are empty.
     */
    public static <T> T[] concat(T[] first, T[] second) {
        // Create a copy of the first array into an array large enough
        // to hold the first and second array.
        T[] result =
            Arrays.copyOf(first,
                          first.length + second.length);

        // Copy the second array at the end of the first array.
        System.arraycopy(second, 
                         0, 
                         result,
                         first.length,
                         second.length);

        // Return the result.
        return result;
    }

    /**
     * Concatenate the contents of two lists and return the result as
     * an array.
     *
     * @return The concatenated contents of two lists as an array or
     * null if both lists are empty.
     */
    public static <T> T[] concat(List<T> first,
                                 List<T> second) {
        List<T> copy;

        if (first.size() != 0)
            copy = first;
        else if (second.size() != 0)
            copy = second;
        else 
            return null;

        @SuppressWarnings("unchecked")
        // Create an array of the appropriate type.
        T[] t = (T[]) java.lang.reflect.Array.newInstance
            (copy.get(0).getClass(),
             0);

        // Convert the list contents to arrays and concatenate them.
        return concat(first.toArray(t), 
                      second.toArray(t));
    }
}
