package utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * This generic custom collector accumulates input elements of type
 * {@code T} into a multimap, which is a {@link Map} parameterized
 * with {@code K} and {@code Collection<V>} types and returns that
 * {@link Map}.
 */
public class MultimapCollector<T, K, V>
       implements Collector<T,
                            Map<K, Collection<V>>,
                            Map<K, Collection<V>>> {
    /**
     * A {@link Function} that maps the key {@code K}.
     */
    private final Function<? super T, ? extends K> mKeyMapper;

    /**
     * A {@link Function} that maps the value {@code V}.
     */
    private final Function<? super T, ? extends V> mValueMapper;

    /**
     * A {@link Supplier} that returns a new, empty {@link Map} into
     * which the results will be inserted.
     */
    private final Supplier<Map<K, Collection<V>>> mMapSupplier;

    /**
     * This static factory method creates a concurrent {@link
     * Collector} that accumulates elements into a {@link Map} whose
     * keys and values are the result of applying the provided mapping
     * functions to the input elements.
     *
     * If the mapped keys contain duplicates (according to {@link
     * Object#equals(Object)}), the value mapping function is applied
     * to each equal element, and the results are merged into the
     * {@link Collection<V>}.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <V> the output type of the value mapping function
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mapSupplier a supplier that returns a new, empty {@link
     *                    Map} into which the results will be inserted
     * @return a {@link Collector} that collects elements into a
     *         {@link Map} whose keys are the result of applying a key
     *         mapping function to the input elements and whose values
     *         are the result of applying a value mapping function to
     *         all input elements equal to the key
     */
    public static <T, K, V> Collector<T, ?, Map<K, Collection<V>>>
        toMap(Function<? super T, ? extends K> keyMapper,
              Function<? super T, ? extends V> valueMapper,
              Supplier<Map<K, Collection<V>>> mapSupplier) {
        return new MultimapCollector<>
            (keyMapper,
             valueMapper,
             mapSupplier);
    }
    
    /**
     * Create a {@link Collector} that accumulates elements into a
     * {@link Map} whose keys and values are the result of applying
     * the provided mapping functions to the input elements.
     * 
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mapSupplier a supplier that returns a new, empty {@link
     *                    Map} into which the results will be inserted
     */
    public MultimapCollector
        (Function<? super T, ? extends K> keyMapper,
         Function<? super T, ? extends V> valueMapper,
         Supplier<Map<K, Collection<V>>> mapSupplier) {
        mKeyMapper = keyMapper;
        mValueMapper = valueMapper;
        mMapSupplier = mapSupplier;
    }

    /**
     * A factory method that creates and returns a new mutable result
     * container of type {@link Map} that holds all the elements in
     * the stream.
     *
     * @return A {@link Supplier} that returns a new, mutable result
     *         container
     */
    @Override
    public Supplier<Map<K, Collection<V>>> supplier() {
        return mMapSupplier;
    }

    /**
     * A method that folds an element into the {@link Map}.
     *
     * @return A {@link BiConsumer} that folds a value into the
     *         mutable result container
     */
    @Override
    public BiConsumer<Map<K, Collection<V>>, T> accumulator() {
        // Return a BiConsumer that takes a Map and an element of type
        // T.
        return (map, t) -> {
            // Apply the key mapping function to the element to obtain
            // its key.
            K key = mKeyMapper.apply(t);

            // Retrieve or create the collection of values
            // corresponding to the key.
            Collection<V> values = map
                .computeIfAbsent(key,
                                 k -> new ArrayList<>());

            // Apply the value mapping function to the element and add
            // the result to the collection.
            values.add(mValueMapper.apply(t));
        };
    }

    /**
     * A method that accepts two partial results and merges them.
     *
     * @return A {@link BinaryOperator} that merges two maps together
     */
    @Override
    public BinaryOperator<Map<K, Collection<V>>> combiner() {
        // Merge the two maps together.
        return (first, second) -> {
            first.putAll(second);
            return first;
        };
    }

    /**
     * This method is a no-op given the {@link Characteristics} set as
     * {@code IDENTITY_FINISH}.
     *
     * @return {@link Function#identity()}
     */
    @Override
    public Function<Map<K, Collection<V>>, Map<K, Collection<V>>> finisher() {
        return Function.identity();
    }

    /**
     * Returns a {@link Set} of {@link Collector.Characteristics}
     * indicating the characteristics of this Collector.  This {@link
     * Set} is immutable.
     *
     * @return An immutable {@link Set} of {@link Collector}
     *         characteristics, which in this case is {@code
     *         [UNORDERED|IDENTITY_FINISH]}
     */
    @Override
    public Set<Characteristics> characteristics() {
        return Collections
            .unmodifiableSet(EnumSet
                             .of(Collector.Characteristics.IDENTITY_FINISH,
                                 Collector.Characteristics.UNORDERED));
    }
}


