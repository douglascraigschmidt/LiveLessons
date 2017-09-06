package utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result =
            Arrays.copyOf(first,
                          first.length + second.length);
        System.arraycopy(second, 
                         0, 
                         result,
                         first.length,
                         second.length);
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

        @SuppressWarnings("unchecked") T[] t = (T[])
                java.lang.reflect.Array.newInstance(copy.get(0).getClass(),
                                                    0);

        return concat(first.toArray(t), second.toArray(t));
    }
}
