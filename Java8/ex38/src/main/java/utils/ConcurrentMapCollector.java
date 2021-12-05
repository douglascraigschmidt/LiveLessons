package utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * A concurrent collector that accumulates input elements of type {@code T}
 * into a {@link ConcurrentHashMap} parameterized with {@code K} and {@code V}
 * types and returns a type {@link M} that extends {@link Map}.
 */
public class ConcurrentMapCollector<T, K, V, M extends Map<K, V>>
       implements Collector<T,
                            Map<K, V>,
                            M> {
    /**
     * A {@link Function} that maps the key {@code K}.
     */
    private final Function<? super T, ? extends K> mKeyMapper;

    /**
     * A {@link Function} that maps the value {@code V}.
     */
    private final Function<? super T, ? extends V> mValueMapper;

    /**
     * A merge function, used to resolve collisions between values
     * associated with the same key, as supplied to {@link
     * Map#merge(Object, Object, BiFunction)}
     */
    private final BinaryOperator<V> mMergeFunction;

    /**
     * A {@link Supplier} that returns a new, empty {@link Map} into
     * which the results will be inserted.
     */
    private final Supplier<M> mMapSupplier;

    /**
     * Create a {@link Collector} that accumulates elements into a
     * {@link Map} whose keys and values are the result of applying
     * the provided mapping functions to the input elements.
     * 
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mergeFunction a merge function, used to resolve collisions between
     *                      values associated with the same key, as supplied
     *                      to {@link Map#merge(Object, Object, BiFunction)}
     * @param mapSupplier a supplier that returns a new, empty {@link
     *                    Map} into which the results will be inserted
     */
    public ConcurrentMapCollector(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends V> valueMapper,
                                  BinaryOperator<V> mergeFunction,
                                  Supplier<M> mapSupplier) {
        mKeyMapper = keyMapper;
        mValueMapper = valueMapper;
        mMergeFunction = mergeFunction;
        mMapSupplier = mapSupplier;
    }

    /**
     * A factory method that creates and returns a new mutable result
     * container of type {@link ConcurrentHashMap} that holds all the elements
     * in the stream.
     *
     * @return a function which returns a new, mutable result container
     */
    @Override
    public Supplier<Map<K, V>> supplier() {
        return ConcurrentHashMap::new;
    }

    /**
     * A method that folds an element into the {@link ConcurrentHashMap}.
     *
     * @return a function that folds a value into a mutable result container
     */
    @Override
    public BiConsumer<Map<K, V>, T> accumulator() {
        return (Map<K, V> map, T element) -> map
            // Add element to the map, handling duplicates in
            // accordance with the mergeFunction.
            .merge(mKeyMapper.apply(element),
                   mValueMapper.apply(element),
                   mMergeFunction);
    }

    /**
     * A method that accepts two partial results and merges them.
     *
     * @return A {@link BinaryOperator} that merges two maps together
     */
    @Override
    public BinaryOperator<Map<K, V>> combiner() {
        // Merge the two maps together.
        return (first, second) -> {
            first.putAll(second);
            return first;
        };
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type {@link ConcurrentHashMap} to the final
     * result type {@code M}, which extends {@link Map}.
     *
     * @return A {@link Map} containing the contents of the stream
     */
    @Override
    public Function<Map<K, V>, M> finisher() {
        return map -> {
            // Create the appropriate map.
            M newMap = mMapSupplier.get();

            // Check whether we've been instantiated to return a
            // ConcurrentHashMap, in which case there's no need to
            // convert anything!
            if (newMap instanceof ConcurrentHashMap)
                //noinspection unchecked
                return (M) map;
            else {
                // Put the contents of the map mutable result
                // container into the new map.
                newMap.putAll(map);

                // Return the new map.
                return newMap;
            }
        };
    }

    /**
     * Returns a {@link Set} of {@link Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * is immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is [UNORDERED|CONCURRENT].
     */
    @Override
    public Set<Characteristics> characteristics() {
        return Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                        Collector.Characteristics.UNORDERED));
    }

    /**
     * This static factory method creates a concurrent {@link
     * Collector} that accumulates elements into a {@link Map} whose
     * keys and values are the result of applying the provided mapping
     * functions to the input elements.
     * 
     * If the mapped keys contains duplicates (according to {@link
     * Object#equals(Object)}), the value mapping function is applied
     * to each equal element, and the results are merged using the
     * provided merging function.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <V> the output type of the value mapping function
     * @param <M> the type of the resulting {@code Map}
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mapSupplier a supplier that returns a new, empty {@link
     *                    Map} into which the results will be inserted
     * @param mergeFunction a merge function, used to resolve collisions between
     *                      values associated with the same key, as supplied
     *                      to {@link Map#merge(Object, Object, BiFunction)}
     * @return a {@link Collector} that collects elements into a
     *        {@link Map} whose keys are the result of applying a key
     *        mapping function to the input elements and whose values
     *        are the result of applying a value mapping function to
     *        all input elements equal to the key
     */
    public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M>
    toMap(Function<? super T, ? extends K> keyMapper,
          Function<? super T, ? extends V> valueMapper,
          BinaryOperator<V> mergeFunction,
          Supplier<M> mapSupplier) {
        return new ConcurrentMapCollector<>
            (keyMapper,
             valueMapper,
             mergeFunction,
             mapSupplier);
    }
}


