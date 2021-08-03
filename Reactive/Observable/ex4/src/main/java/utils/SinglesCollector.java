package utils;

import io.reactivex.rxjava3.core.Single;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Implements a custom collector that converts a stream of RxJava
 * {@link Single} objects into one {@link Single} object that is
 * triggered when all {@link Single} objects in the stream complete.
 */
public class SinglesCollector<T>
      implements Collector<Single<T>,
                           List<Single<T>>,
                           Single<List<T>>> {
    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the singles in the stream.
     *
     * @return a function which returns a new, mutable result
     * container
     */
    @Override
    public Supplier<List<Single<T>>> supplier() {
        return ArrayList::new;
    }

    /**
     * A function that folds a single into the mutable result container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<List<Single<T>>, Single<T>> accumulator() {
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
    public BinaryOperator<List<Single<T>>> combiner() {
        return (List<Single<T>> one,
                List<Single<T>> another) -> {
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
    public Function<List<Single<T>>, Single<List<T>>> finisher() {
        // This Function combines results from Single.zipArray().
        @SuppressWarnings("unchecked")
        io.reactivex.rxjava3.functions.Function<Object[], List<T>> combiner = 
            bfArray -> Stream
                // Create a stream of Objects.
                .of(bfArray)

                // Convert the Objects to T.
                .map((Object o) -> (T) o)

                // Collect the results together.
                .collect(toList());

        // Return a new Single that completes when all Single objects
        // in the List complete.
        return singles -> Single
            .zipArray(combiner,
                      singles.toArray(new Single[0]));
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
    public Set<Characteristics> characteristics() {
        return Collections.singleton(Characteristics.UNORDERED);
    }

    /**
     * This static factory method creates a new SinglesCollector.
     *
     * @return A new SinglesCollector
     */
    public static <T> Collector<Single<T>, ?, Single<List<T>>>
        toSingle() {
        return new SinglesCollector<T>();
    }
}
