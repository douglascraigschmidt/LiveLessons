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
public class MonosCollector<T>
      implements Collector<Mono<T>,
                           List<Mono<T>>,
                           Mono<List<T>>> {
    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the monos in the stream.
     *
     * @return a function which returns a new, mutable result
     * container
     */
    @Override
    public Supplier<List<Mono<T>>> supplier() {
        return ArrayList::new;
    }

    /**
     * A function that folds a mono into the mutable result container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<List<Mono<T>>, Mono<T>> accumulator() {
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
    public BinaryOperator<List<Mono<T>>> combiner() {
        return (List<Mono<T>> one,
                List<Mono<T>> another) -> {
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
    public Function<List<Mono<T>>, Mono<List<T>>> finisher() {
        // Return a mono to a list of completed elements of type T.
        return monos -> Mono
            // Return a new mono that completes when all monos in the
            // list complete.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#when-java.lang.Iterable-
            .when(monos)

            // Return a mono that signals when all the monos complete.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#materialize--
            .materialize()

            // When all monos have completed get a single mono to a
            // list of elements of type T.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#flatMap-java.util.function.Function-
            .flatMap(v -> Flux
                     // Create a flux stream of completable monos.
                     // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#fromIterable-java.lang.Iterable-
                     .fromIterable(monos)

                     // Use map() to block() all monos and yield
                     // objects of type T (block() will not
                     // actually block).
                     // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-
                     .map(Mono::block)

                     // Collect the results of type T into a list.
                     // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#collect-java.util.stream.Collector-
                     .collect(toList()));
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
     * This static factory method creates a new MonosCollector.
     *
     * @return A new MonosCollector
     */
    public static <T> Collector<Mono<T>, ?, Mono<List<T>>>
                          toMono() {
        return new MonosCollector<T>();
    }
}
