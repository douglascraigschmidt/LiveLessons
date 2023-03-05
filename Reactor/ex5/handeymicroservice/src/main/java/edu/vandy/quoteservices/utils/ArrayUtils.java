package edu.vandy.quoteservices.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayUtils {
    /**
     *
     * @param obj
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends Number> List<T> obj2Vector(Object obj,
                                                        Class<T> clazz) {
        if (obj == null || !obj.getClass().isArray()) {
            return Collections.emptyList();
        } else {
            return Arrays
                .stream((Object[]) obj)
                .map(Object::toString)
                .map(str -> convertToNumber(str, clazz))
                .filter(Objects::nonNull)
                .map(clazz::cast)
                .toList();
        }
    }

    /**
     *
     * @param obj
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends Number> T obj2Number(Object obj,
                                                        Class<T> clazz) {
        if (obj == null) {
            return null;
        } else {
            return Stream
                .of(obj)
                .map(Object::toString)
                .map(str -> convertToNumber(str, clazz))
                .filter(Objects::nonNull)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
        }
    }

    /**
     *
     * @param str
     * @param clazz
     * @return
     * @param <T>
     */
    private static <T extends Number> T convertToNumber(String str,
                                                        Class<T> clazz) {
        try {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(str));
            } else if (clazz == Long.class) {
                return clazz.cast(Long.valueOf(str));
            } else if (clazz == Float.class) {
                return clazz.cast(Float.valueOf(str));
            } else if (clazz == Double.class) {
                return clazz.cast(Double.valueOf(str));
            } else if (clazz == Short.class) {
                return clazz.cast(Short.valueOf(str));
            } else if (clazz == Byte.class) {
                return clazz.cast(Byte.valueOf(str));
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
