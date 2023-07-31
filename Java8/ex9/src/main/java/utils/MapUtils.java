package utils;

import java.util.Comparator;
import java.util.Map;
import java.util.LinkedHashMap;

import static java.util.stream.Collectors.toMap;

public class MapUtils<K, V> {
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
        // Create a map that's sorted by the value in map.
        return map
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Sort the elements in the stream using the comparator.
            .sorted(comparator)

            // Trigger intermediate processing and collect key/value
            // pairs in the stream into a LinkedHashMap, which
            // preserves the sorted order.
            .collect(toMap(Map.Entry::getKey,
                           Map.Entry::getValue,
                           (e1, e2) -> e2,
                           LinkedHashMap::new));
    }
}
