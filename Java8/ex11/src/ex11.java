import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the improper use of the {@code Stream.peek()}
 * intermediate operation to interfere with a running stream.
 */
public class ex11 {
    static public void main(String[] argv) {
        boolean useSequentialStream = argv.length > 0;

        // Indicate how the test is being run.
        System.out.println("Beginning test with a "
                           + (useSequentialStream ? "sequential" : "parallel")
                           + " stream");

        // Create a list of 10 integers in the range [0..10).
        List<Integer> list = IntStream
            .range(0, 10)
            .boxed()
            .collect(toList());

        Stream<Integer> stream = list
            // Convert the list of integers into a stream of integers.
            .stream();

        if (!useSequentialStream)
            stream
                // Conditionally convert the sequential stream into
                // parallel stream.
                .parallel();

        stream
            // Improperly modify the stream during its processing,
            // which generates a ConcurrentModificationException.
            .peek(list::remove)

            // Print out the results of the stream.
            .forEach(ex11::display);
    }

    /**
     * Print the {@link Integer}.
     */
    private static void display(Integer integer) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + integer);
    }
}

