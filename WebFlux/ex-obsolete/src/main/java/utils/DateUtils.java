package utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * This Java utility class contains a method that converts from a
 * String format into a Date format.
 */
public final class DateUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private DateUtils() {
    }

    /**
     * Converts the {@code date} string into a {@code Date} object.
     *
     * @param date The string to convert into a {@code Date} object
     * @return A {@code Date} object representation of the {@code date} string
     */
    public static Date parse(String date) {
        return Timestamp.valueOf(LocalDateTime.parse(date));
    }
}
