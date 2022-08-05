package utils;

import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This Java utility class efficiently finds the top n items in a
 * {@link Flux} using a Java {@link PriorityQueue}, which is
 * implemented internally via a Heap data structure.
 */
public class GetTopN {
    /**
     * A Java utility class should have a private constructor.
     */ 
    private GetTopN() {}

    /**
     * This adapter method enables integration with Project Reactor's
     * {@code transform()} operator.
     *
     * @param n The max number of items to return
     * @return A {@link Flux} that emits the top {@code n}
     *         items in the implicit {@link Flux parameter}
     */
    public static Function<Flux<Integer>, Flux<Integer>> getTopN(int n) {
        return flux -> GetTopN.getTopN(flux, n);
    }

    /**
     * This method returns a {@link Flux} that emits the top {@code n}
     * items in the {@code flux} parameter.
     *
     * @param flux The {@link Flux} given as input
     * @param n The max number of items to return
     * @return A {@link Flux} that emits the top {@code n}
     *         items in the {@code flux} parameter
     */
    public static Flux<Integer> getTopN(Flux<Integer> flux,
                                        int n) {
        if (n < 1)
            // Return an empty Flux if n < 1.
            return Flux.empty();
        else {
            // Create a new heap to efficiently keep track of the top
            // n items.
            Queue<Integer> heap = new PriorityQueue<>();

            return flux
                // Return an empty Flux if there's no input.
                .switchIfEmpty(Flux.empty())

                // This action operator updates the heap.
                .doOnNext(updateHeap(n, heap))

                // After all the updateHeap() processing completes
                // convert the heap into a Flux whose n values are
                // ordered from highest to lowest.
                .thenMany(Flux.defer(() -> getTopN(heap)));
        }
    }

    /**
     * Returns a {@link Consumer} that updates the heap.
     *
     * @param n The max number of items to return
     * @param heap The heap used to keep track of the top {@code n}
     *             items
     * @return A {@link Consumer} that updates the heap
     */
    private static Consumer<Integer> updateHeap(int n,
                                                Queue<Integer> heap) {
        return elem -> {
            heap.offer(elem);

            if (heap.size() > n)
                heap.poll();
        };
    }

    /**
     * Returns a {@link Flux} that contains the items in the queue
     * sorted from highest to lowest.
     *
     * @param heap The heap containing the top n items
     * @return A {@link Flux} that contains the items in the queue
     *         sorted from highest to lowest
     */
    private static Flux<Integer> getTopN(Queue<Integer> heap) {
        return Flux
            // Convert the Queue into a Flux.
            .fromIterable(heap)

            // Sort the Flux contents from highest to lowest.
            .sort(Comparator.reverseOrder());
    }
}
