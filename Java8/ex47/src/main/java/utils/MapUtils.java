package utils;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * This Java utility class defines method(s) that provide operations
 * on {@link Map} collections.
 */
public class MapUtils<K, V> {
    /**
     * A Java utility class should define a private constructor.
     */
    private MapUtils() { }

    /**
     * Sort {@code map} via the {@code comparator}.
     *
     * @param map The map to sort
     * @param comparator The comparator to compare map entries
     * @return The sorted map
     */
    public static <K, V> Map<K, V> sortMap
        (Map<K, V> map,
         Comparator<Map.Entry<K, V>> comparator) {
        // Create a list from the map's entrySet.
        List<Map.Entry<K, V>> entries =
            new ArrayList<>(map.entrySet());

        // Sort the list using the provided comparator.
        entries.sort(comparator);

        // Create a LinkedHashMap that will preserve the sorted order.
        Map<K, V> sortedMap = new LinkedHashMap<>();

        // Add the sorted entries to the LinkedHashMap.
        for (Map.Entry<K, V> entry : entries)
            sortedMap.put(entry.getKey(), entry.getValue());

        // Return the sorted map.
        return sortedMap;
    }
}
