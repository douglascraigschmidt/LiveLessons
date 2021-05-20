package utils;

import datamodels.Flight;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Define a collector that converts a stream of TripResponses into a
 * List that emits the cheapest priced trips(s).
 */
public class CheapestPriceCollector
             implements Collector<Flight,
                                  List<Flight>,
                                  List<Flight>> {
    /**
     * The minimum value seen by the collector.
     */
    Double mMin = Double.MAX_VALUE;

    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the TripResponses in the stream.
     *
     * @return a function which returns a new, mutable result container
     */
    @Override
    public Supplier<List<Flight>> supplier() {
        return ArrayList::new;
    }

    /**
     * A function that folds a TripResponse into the mutable result
     * container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<List<Flight>, Flight> accumulator() {
        return (lowestPrices, tripResponse) -> {
            // If the price of the trip is less than the current min
            // Add it to the lowestPrices List and update the current
            // min price.
            if (tripResponse.getPrice() < mMin) {
                lowestPrices.clear();
                lowestPrices.add(tripResponse);
                mMin = tripResponse.getPrice();

            // If the price of the trip is equal to the current min
            // add it to the lowestPrices List.
            } else if (tripResponse.getPrice().equals(mMin)) {
                lowestPrices.add(tripResponse);
            }
        };
    }

    /**
     * A function that accepts two partial results and merges them.
     * The combiner function may fold state from one argument into the
     * other and return that, or may return a new result container.
     *
     * @return a function which combines two partial results into a
     * combined result
     */
    @Override
    public BinaryOperator<List<Flight>> combiner() {
        // Merge two Lists together.
        return (one, another) -> {
            one.addAll(another);
            return one;
        };
    }

    /**
     * This method is a no-op.
     */
    @Override
    public Function<List<Flight>, List<Flight>> finisher() {
        return null;
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is [UNORDERED|IDENTITY_FINISH].
     */
    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                                                      Collector.Characteristics.IDENTITY_FINISH));
    }

    /**
     * This static factory method creates a new
     * CheapestFlightCollector.
     *
     * @return A new CheapestFlightCollector()
     */
    public static Collector<Flight, List<Flight>, List<Flight>>
    toList() {
        return new CheapestPriceCollector();
    }
}

