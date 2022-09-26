package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

/**
 * Implements a custom collector that converts a stream of Reactor
 * Mono objects into a single Mono object that is triggered when all
 * the monos in the stream complete.
 */
public class FluxCollector<T>
      implements Collector<T,
                           List<T>,
                           List<T>> {
    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the monos in the stream.
     *
     * @return a function which returns a new, mutable result
     * container
     */
    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    /**
     * A function that folds a mono into the mutable result container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return Collection::add;
    }

    /**
     * A function that accepts two partial results and merges them.
     * The combiner function may fold state from one argument into the
     * other and return that, or may return a new result container.
     *
     * @return a function which combines two partial results into a combined
     * result
     */
    @Override
    public BinaryOperator<List<T>> combiner() {
        return (List<T> one,
                List<T> another) -> {
            one.addAll(another);
            return one;
        };
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type {@code A} to the final result type {@code R}.
     *
     * @return a function which transforms the intermediate result to
     * the final result
     */
    @Override
    public Function<List<T>, List<T>> finisher() {
        // Return a mono to a list of completed elements of type T.
        return Function.identity();
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is simply UNORDERED
     */
    @Override
    public Set characteristics() {
        return Collections.singleton(Characteristics.UNORDERED);
    }

    /**
     * This static factory method creates a new MonoCollector.
     *
     * @return A new FuturesCollector
     */
    public static <T> Collector<T, ?, List<T>>
        toList() {
        return new FluxCollector<T>();
    }
}
