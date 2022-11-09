package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Implements a custom collector that converts a stream of {@link
 * CompletableFuture<Integer>} objects into a single {@link
 * CompletableFuture<IntStream>} that is triggered when all the {@link
 * CompletableFuture<Integer>} objects in the {@link Stream} complete.
 *
 * See
 * <a href="http://www.nurkiewicz.com/2013/05/java-8-completablefuture-in-action.html">this link</a>
 * for more info.
 */
public class FuturesCollectorIntStream
      implements Collector<CompletableFuture<Integer>,
                           List<CompletableFuture<Integer>>,
                           CompletableFuture<IntStream>> {
    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the CompletableFutures in the
     * stream.
     *
     * @return a function which returns a new, mutable result container
     */
    @Override
    public Supplier<List<CompletableFuture<Integer>>> supplier() {
        return ArrayList::new;
    }

    /**
     * A function that folds a CompletableFuture into the mutable
     * result container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<List<CompletableFuture<Integer>>, CompletableFuture<Integer>>
        accumulator() {
        return List::add;
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
    public BinaryOperator<List<CompletableFuture<Integer>>> combiner() {
        return (List<CompletableFuture<Integer>> one,
                List<CompletableFuture<Integer>> another) -> {
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
    public Function<List<CompletableFuture<Integer>>, CompletableFuture<IntStream>>
        finisher() {
        return futures
            -> CompletableFuture
            // Use CompletableFuture.allOf() to obtain a
            // CompletableFuture that will itself complete when all
            // CompletableFutures in futures have completed.
            .allOf(futures.toArray(new CompletableFuture[0]))

            // When all futures have completed get a CompletableFuture
            // to an array of joined elements of type T.
            .thenApply(v -> futures
                       // Convert futures into a stream of completable
                       // futures.
                       .stream()

                       // Use map() to join() all completable futures
                       // and yield objects of type T.  Note that
                       // join() should never block.
                       .mapToInt(CompletableFuture::join));
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
     * This static factory method creates a new FuturesCollector.
     *
     * @return A new FuturesCollector()
     */
    public static Collector<CompletableFuture<Integer>, ?, CompletableFuture<IntStream>>
        toFuture() {
        return new FuturesCollectorIntStream();
    }
}
